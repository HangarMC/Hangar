<script lang="ts" setup>
import * as webauthnJson from "@github/webauthn-json";
import type { LoginResponse } from "~/types/backend";

const route = useRoute("auth-login");
const router = useRouter();
const authStore = useAuthStore();
const v = useVuelidate();
const notification = useNotificationStore();
const i18n = useI18n();
const backendData = useBackendData;

const loading = ref(false);
const supportedMethods = ref<string[]>([]);

const returnUrl = computed(() => (route.query.returnUrl as string) || "/auth/settings/profile");

// aal1
const username = ref("");
const password = ref("");

const privileged = (route.query.privileged as unknown as boolean) || false;
if (privileged) {
  username.value = useAuthStore().user?.name || "";
  loading.value = true;
  const response = await useInternalApi<LoginResponse>("auth/login/sudo", "POST");
  if (response.types?.length) {
    supportedMethods.value.push(...response.types);
  }
  loading.value = false;
}

async function loginPassword() {
  if (!(await v.value.$validate())) return;
  loading.value = true;
  try {
    const response = await useInternalApi<LoginResponse>("auth/login/password", "POST", {
      usernameOrEmail: username.value,
      password: password.value,
    });
    if (response.types?.length) {
      supportedMethods.value.push(...response.types);
    } else {
      await finish(response);
    }
  } catch (err) {
    notification.fromError(i18n, err);
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
    const response = await useInternalApi<LoginResponse>("auth/login/webauthn", "POST", {
      usernameOrEmail: username.value,
      password: password.value,
      publicKeyCredentialJson: JSON.stringify(publicKeyCredential),
    });
    await finish(response);
  } catch (err) {
    if (err?.toString()?.startsWith("NotAllowedError")) {
      notification.error("Security Key Authentication failed!");
    } else {
      notification.fromError(i18n, err);
    }
  }
  loading.value = false;
}

const totpCode = ref();

async function loginTotp() {
  loading.value = true;
  try {
    const response = await useInternalApi<LoginResponse>("auth/login/totp", "POST", {
      usernameOrEmail: username.value,
      password: password.value,
      totpCode: totpCode.value,
    });
    await finish(response);
  } catch (err) {
    notification.fromError(i18n, err);
  }
  loading.value = false;
}

const backupCode = ref();

async function loginBackupCode() {
  loading.value = true;
  try {
    const response = await useInternalApi<LoginResponse>("auth/login/backup", "POST", {
      usernameOrEmail: username.value,
      password: password.value,
      backupCode: backupCode.value,
    });
    await finish(response);
  } catch (err) {
    notification.fromError(i18n, err);
  }
  loading.value = false;
}

async function finish(response: LoginResponse) {
  if (response.aal !== undefined && response.user?.accessToken) {
    authStore.aal = response.aal;
    authStore.user = response.user;
    authStore.authenticated = true;
    authStore.invalidated = false;
    authStore.token = response.user.accessToken;
    await router.push(returnUrl.value);
  } else {
    notification.error("Did not receive user?");
  }
}

useSeo(computed(() => ({ title: "Login", route })));
</script>

<template>
  <Card class="w-xl mx-auto max-w-full">
    <template #header>{{ privileged ? "Sudo" : "Login" }}</template>

    <div v-if="privileged" class="mb-2">Please authenticate again to do this action</div>

    <form v-if="supportedMethods.length === 0" class="flex flex-col gap-2">
      <InputText
        v-model="username"
        label="Username"
        name="username"
        autocomplete="username"
        :rules="[required()]"
        :class="{ 'hidden!': privileged }"
        :disabled="privileged"
      />
      <InputPassword v-model="password" label="Password" name="password" autocomplete="current-password" :rules="[required()]" />
      <div class="flex flex-col gap-2">
        <Button :disabled="loading" @click.prevent="loginPassword">Login</Button>
        <template v-if="!privileged">
          <div class="flex items-center space-x-2">
            <hr class="flex-grow border-zinc-200 dark:border-zinc-700" />
            <span class="text-zinc-400 dark:text-zinc-300 text-sm">OR</span>
            <hr class="flex-grow border-zinc-200 dark:border-zinc-700" />
          </div>
          <div class="flex flex-row justify-center gap-x-4">
            <Button
              v-for="provider in backendData.security.oauthProviders"
              :key="provider"
              :disabled="loading"
              :href="'/api/internal/oauth/' + provider + '/login?mode=login&returnUrl=' + returnUrl"
            >
              <template v-if="provider === 'github'">
                <div class="flex flex-row gap-x-0.5 items-center">
                  <IconMdiGithub class="mr-1" />
                  GitHub
                </div>
              </template>
              <template v-if="provider === 'google'">
                <div class="flex flex-row gap-x-0.5 items-center">
                  <IconMdiGoogle class="mr-1" />
                  Google
                </div>
              </template>
              <template v-if="provider === 'microsoft'">
                <div class="flex flex-row gap-x-0.5 items-center">
                  <IconMdiMicrosoft class="mr-1" />
                  Microsoft
                </div>
              </template>
            </Button>
          </div>
        </template>
      </div>

      <hr class="flex-grow border-zinc-200 dark:border-zinc-700 mt-1" />

      <Link v-if="!privileged" button-type="secondary" to="/auth/signup" class="w-max">Don't have an account yet? Create one!</Link>
      <Link v-if="!privileged" to="/auth/reset" class="w-max">Forgot your password?</Link>
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
