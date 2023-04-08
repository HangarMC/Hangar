<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useRoute, useRouter } from "vue-router";
import { ref } from "vue";
import * as webauthnJson from "@github/webauthn-json";
import { useVuelidate } from "@vuelidate/core";
import { useI18n } from "vue-i18n";
import { useSeo } from "~/composables/useSeo";
import InputText from "~/components/ui/InputText.vue";
import Button from "~/components/design/Button.vue";
import { useInternalApi } from "~/composables/useApi";
import { useAuthStore } from "~/store/auth";
import { useAuth } from "~/composables/useAuth";
import InputPassword from "~/components/ui/InputPassword.vue";
import Card from "~/components/design/Card.vue";
import Link from "~/components/design/Link.vue";
import { required } from "~/composables/useValidationHelpers";
import { useNotificationStore } from "~/store/notification";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const v = useVuelidate();
const notification = useNotificationStore();
const i18n = useI18n();

const loading = ref(false);
const supportedMethods = ref([]);

// aal1
const username = ref("");
const password = ref("");

async function loginPassword() {
  if (!(await v.value.$validate())) return;
  loading.value = true;
  try {
    const response = await useInternalApi<{ aal: number; types: string[] }>("auth/login/password", "POST", {
      usernameOrEmail: username.value,
      password: password.value,
    });
    if (response.types?.length > 0) {
      supportedMethods.value.push(...response.types);
    } else {
      await finish(response.aal);
    }
  } catch (e) {
    notification.fromError(i18n, e);
  }
  loading.value = false;
}

// aal2

async function loginWebAuthN() {
  loading.value = true;
  try {
    const credentialGetOptions = await useInternalApi<string>("auth/webauthn/assert", "POST", username.value, { headers: { "content-type": "text/plain" } });
    const parsed = JSON.parse(credentialGetOptions);
    const publicKeyCredential = await webauthnJson.get(parsed);
    const response = await useInternalApi<{ aal: number }>("auth/login/webauthn", "POST", {
      usernameOrEmail: username.value,
      password: password.value,
      publicKeyCredentialJson: JSON.stringify(publicKeyCredential),
    });
    await finish(response.aal);
  } catch (e) {
    notification.fromError(i18n, e);
  }
  loading.value = false;
}

const totpCode = ref();

async function loginTotp() {
  loading.value = true;
  try {
    const response = await useInternalApi<{ aal: number }>("auth/login/totp", "POST", {
      usernameOrEmail: username.value,
      password: password.value,
      totpCode: totpCode.value,
    });
    await finish(response.aal);
  } catch (e) {
    notification.fromError(i18n, e);
  }
  loading.value = false;
}

const backupCode = ref();

async function loginBackupCode() {
  loading.value = true;
  try {
    const response = await useInternalApi<{ aal: number }>("auth/login/backup", "POST", {
      usernameOrEmail: username.value,
      password: password.value,
      backupCode: backupCode.value,
    });
    await finish(response.aal);
  } catch (e) {
    notification.fromError(i18n, e);
  }
  loading.value = false;
}

async function finish(aal: number) {
  authStore.aal = aal;
  await useAuth.updateUser(); // todo maybe return user in login response?
  const returnUrl = (route.query.returnUrl as string) || "/auth/settings/profile";
  await router.push(returnUrl);
}

useHead(useSeo("Login", null, route, null));
</script>

<template>
  <Card>
    <template #header> Login</template>

    <form v-if="supportedMethods.length === 0" class="flex flex-col gap-2">
      <InputText v-model="username" label="Username" name="username" autocomplete="username" :rules="[required()]" />
      <InputPassword v-model="password" label="Password" name="password" autocomplete="current-password" :rules="[required()]" />
      <div class="flex gap-2">
        <Button :disabled="loading" @click.prevent="loginPassword">Login</Button>
        <Button button-type="secondary" to="/auth/signup">Signup</Button>
      </div>
      <Link to="/auth/reset" class="w-max">Forgot your password?</Link>
    </form>

    <form v-if="supportedMethods.length > 0" class="flex flex-col gap-2 hide-last-hr">
      <p>Please verify your sign in using one of your second factors</p>
      <template v-if="supportedMethods.includes('WEBAUTHN')">
        <Button class="w-max" :disabled="loading" @click.prevent="loginWebAuthN">Use WebAuthN</Button>
        <hr />
      </template>
      <template v-if="supportedMethods.includes('TOTP')">
        <div class="flex flex-col gap-2">
          <InputText v-model="totpCode" label="Totp code" inputmode="numeric" />
          <Button class="w-max" :disabled="loading" @click.prevent="loginTotp">Use totp</Button>
        </div>
        <hr />
      </template>
      <template v-if="supportedMethods.includes('BACKUP_CODES')">
        <div class="flex flex-col gap-2">
          <InputText v-model="backupCode" label="Backup code" />
          <Button class="w-max" :disabled="loading" @click.prevent="loginBackupCode">Use backup code</Button>
        </div>
        <hr />
      </template>
    </form>
  </Card>
</template>

<style scoped>
.hide-last-hr > hr:last-of-type {
  display: none;
}
</style>
