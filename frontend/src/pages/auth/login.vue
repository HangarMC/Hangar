<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useRoute, useRouter } from "vue-router";
import { ref } from "vue";
import * as webauthnJson from "@github/webauthn-json";
import { useSeo } from "~/composables/useSeo";
import InputText from "~/lib/components/ui/InputText.vue";
import Button from "~/lib/components/design/Button.vue";
import { useInternalApi } from "~/composables/useApi";
import { useAuthStore } from "~/store/auth";
import { useAuth } from "~/composables/useAuth";
import InputPassword from "~/lib/components/ui/InputPassword.vue";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const supportedMethods = ref([]);

// aal1
const username = ref("");
const password = ref("");

async function loginPassword() {
  const response = await useInternalApi<{ aal: number; types: string[] }>("auth/login/password", "POST", {
    usernameOrEmail: username.value,
    password: password.value,
  });
  if (response.types?.length > 0) {
    supportedMethods.value.push(...response.types);
  } else {
    await finish(response.aal);
  }
}

// aal2

async function loginWebAuthN() {
  const credentialGetOptions = await useInternalApi<string>("auth/webauthn/assert", "POST", username.value, { headers: { "content-type": "text/plain" } });
  const parsed = JSON.parse(credentialGetOptions);
  console.log("response", parsed);
  const publicKeyCredential = await webauthnJson.get(parsed);
  const response = await useInternalApi<{ aal: number }>("auth/login/webauthn", "POST", {
    usernameOrEmail: username.value,
    password: password.value,
    publicKeyCredentialJson: JSON.stringify(publicKeyCredential),
  });
  await finish(response.aal);
}

const totpCode = ref();
async function loginTotp() {
  const response = await useInternalApi<{ aal: number }>("auth/login/totp", "POST", {
    usernameOrEmail: username.value,
    password: password.value,
    totpCode: totpCode.value,
  });
  await finish(response.aal);
}

const backupCode = ref();
async function loginBackupCode() {
  const response = await useInternalApi<{ aal: number }>("auth/login/backup", "POST", {
    usernameOrEmail: username.value,
    password: password.value,
    backupCode: backupCode.value,
  });
  await finish(response.aal);
}

async function finish(aal: number) {
  authStore.aal = aal;
  await useAuth.updateUser(); // todo maybe return user in login response?
  // TODO redirect to where you started
  await router.push("/auth/settings");
}

useHead(useSeo("Login", null, route, null));
</script>

<template>
  <div>
    <div v-if="supportedMethods.length === 0">
      <InputText v-model="username" label="Username" name="useranme" />
      <InputPassword v-model="password" label="Password" name="password" autocomplete="current-password" />
      <Button @click.prevent="loginPassword">Login</Button>
      <Button to="/auth/signup">Signup</Button>
      <Button to="/auth/reset">Forgot</Button>
    </div>

    <div v-if="supportedMethods.length > 0">
      <Button v-if="supportedMethods.includes('WEBAUTHN')" @click.prevent="loginWebAuthN">Use WebAuthN</Button>
      <template v-if="supportedMethods.includes('TOTP')">
        <InputText v-model="totpCode" label="Totp code" inputmode="numeric" />
        <Button @click.prevent="loginTotp">Use totp</Button>
      </template>
      <template v-if="supportedMethods.includes('BACKUP_CODES')">
        <InputText v-model="backupCode" label="Backup code" />
        <Button @click.prevent="loginBackupCode">Use backup code</Button>
      </template>
    </div>
  </div>
</template>
