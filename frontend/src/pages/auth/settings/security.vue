<script lang="ts" setup>
import * as webauthnJson from "@github/webauthn-json";
import { type AxiosRequestConfig, isAxiosError } from "axios";
import type { Authenticator, SettingsResponse } from "~/types/backend";

defineProps<{
  settings?: SettingsResponse;
}>();
const emit = defineEmits<{
  refreshSettings: [];
}>();

const auth = useAuthStore();
const notification = useNotificationStore();
const i18n = useI18n();
const { t } = i18n;
const v = useVuelidate();
const router = useRouter();
const route = useRoute("auth-settings");
const backendData = useBackendData;

const loading = ref(false);

const authenticatorName = ref<string>();

async function addAuthenticator() {
  if (!(await v.value.$validate())) return;
  loading.value = true;
  try {
    const credentialCreateOptions = await useInternalApi<string>("auth/webauthn/setup", "POST", authenticatorName.value, {
      headers: { "content-type": "text/plain" },
    });
    const parsed = JSON.parse(credentialCreateOptions);
    const publicKeyCredential = await webauthnJson.create(parsed);
    await useInternalApi("auth/webauthn/register", "POST", JSON.stringify(publicKeyCredential), { headers: { "content-type": "text/plain" } });
    authenticatorName.value = "";
    emit("refreshSettings");
    v.value.$reset();
  } catch (e) {
    if (isAxiosError(e) && e.response?.status === 499) {
      codes.value = e.response.data.body;
      backupCodeModal.value.isOpen = true;
      savedRequest.value = e.config;
    } else if (isAxiosError(e) && e.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else if (e?.toString()?.startsWith("NotAllowedError")) {
      notification.error("Security Key Authentication failed!");
    } else {
      notification.fromError(i18n, e);
    }
  }
  loading.value = false;
}

async function unregisterAuthenticator(authenticator: Authenticator) {
  loading.value = true;
  try {
    await useInternalApi("auth/webauthn/unregister", "POST", authenticator.id, { headers: { "content-type": "text/plain" } });
    emit("refreshSettings");
  } catch (e) {
    if (isAxiosError(e) && e.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else if (e?.toString()?.startsWith("NotAllowedError")) {
      notification.error("Security Key Authentication failed!");
    } else {
      notification.fromError(i18n, e);
    }
  }
  loading.value = false;
}

const newAuthenticatorName = ref<string>();
const currentlyRenamingAuthenticator = ref<Authenticator>();
const authenticatorRenameModal = ref();

function renameAuthenticatorModal(authenticator: Authenticator) {
  newAuthenticatorName.value = authenticator.displayName;
  currentlyRenamingAuthenticator.value = authenticator;
  authenticatorRenameModal.value.isOpen = true;
  v.value.$reset();
}

async function renameAuthenticator() {
  if (!(await v.value.$validate())) return;
  if (!currentlyRenamingAuthenticator.value) {
    notification.error("Something went wrong, please try again");
    return;
  }
  loading.value = true;
  try {
    await useInternalApi("auth/webauthn/rename", "POST", { id: currentlyRenamingAuthenticator.value.id, displayName: newAuthenticatorName.value });
    authenticatorRenameModal.value.isOpen = false;
    emit("refreshSettings");
  } catch (e) {
    if (isAxiosError(e) && e.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else if (e?.toString()?.startsWith("NotAllowedError")) {
      notification.error("Security Key Authentication failed!");
    } else {
      notification.fromError(i18n, e);
    }
  }
  loading.value = false;
}

const totpData = ref<{ secret: string; qrCode: string } | undefined>();

async function setupTotp() {
  loading.value = true;
  try {
    totpData.value = await useInternalApi<{ secret: string; qrCode: string }>("auth/totp/setup", "POST");
  } catch (e) {
    if (isAxiosError(e) && e.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else {
      notification.fromError(i18n, e);
    }
  }
  loading.value = false;
}

const totpCode = ref();

async function addTotp() {
  loading.value = true;
  try {
    await useInternalApi("auth/totp/register", "POST", { secret: totpData.value?.secret, code: totpCode.value });
    totpCode.value = undefined;
    emit("refreshSettings");
  } catch (e) {
    if (isAxiosError(e) && e.response?.status === 499) {
      codes.value = e.response.data.body;
      backupCodeModal.value.isOpen = true;
      savedRequest.value = e.config;
      otp.value = e.response.headers["x-hangar-verify"];
    } else if (isAxiosError(e) && e.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else {
      notification.fromError(i18n, e);
    }
  }
  loading.value = false;
}

async function unlinkTotp() {
  loading.value = true;
  try {
    await useInternalApi("auth/totp/remove", "POST");
    emit("refreshSettings");
  } catch (e) {
    if (isAxiosError(e) && e.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else {
      notification.fromError(i18n, e);
    }
  }
  loading.value = false;
}

const showCodes = ref(false);
const codes = ref();

const savedRequest = ref<AxiosRequestConfig>();
const backupCodeModal = ref();
const backupCodeConfirm = ref();
const otp = ref<string>();

async function confirmAndRepeat() {
  loading.value = true;
  try {
    const req = savedRequest.value;
    if (req) {
      // set header
      let headers = req.headers;
      if (!headers) {
        headers = {};
        req.headers = headers;
      }
      headers["X-Hangar-Verify"] = backupCodeConfirm.value + (otp.value ? ":" + otp.value : "");
      // repeat request
      await useAxios()(req);
      // close modal
      backupCodeConfirm.value = undefined;
      backupCodeModal.value.isOpen = false;
      // reset stuff
      emit("refreshSettings");
      totpCode.value = undefined;
      totpData.value = undefined;
      authenticatorName.value = "";
      notification.success("Successfully enabled 2FA!");
      v.value.$reset();
    } else {
      notification.error("no saved request?");
    }
  } catch (e) {
    notification.fromError(i18n, e);
  }
  loading.value = false;
}

async function revealCodes() {
  loading.value = true;
  try {
    if (!codes.value) {
      codes.value = await useInternalApi("auth/codes/show", "POST");
    }
    showCodes.value = true;
  } catch (e) {
    if (isAxiosError(e) && e.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else {
      notification.fromError(i18n, e);
    }
  }
  loading.value = false;
}

async function generateNewCodes() {
  loading.value = true;
  try {
    codes.value = await useInternalApi("auth/codes/regenerate", "POST");
    notification.success("Regenerated backup codes!");
    emit("refreshSettings");
  } catch (e) {
    if (isAxiosError(e) && e.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else {
      notification.fromError(i18n, e);
    }
  }
  loading.value = false;
}

const currentlyUnlinkingProvider = ref<string>();
const oauthModal = ref();
const unlinkUrl = ref<string>();

async function setupOAuth(provider: string) {
  try {
    window.location.href = await useInternalApi<string>("oauth/" + provider + "/login?mode=settings&returnUrl=" + encodeURIComponent(route.fullPath), "GET");
  } catch (e) {
    if (isAxiosError(e) && e.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else {
      notification.fromError(i18n, e);
    }
  }
}

async function unlinkOAuth(provider: string, id: string) {
  try {
    unlinkUrl.value = await useInternalApi("oauth/" + provider + "/unlink/" + id, "POST");
    currentlyUnlinkingProvider.value = provider;
    oauthModal.value.isOpen = true;
  } catch (e) {
    notification.fromError(i18n, e);
  }
}

function closeUnlinkModal() {
  oauthModal.value.isOpen = false;
  emit("refreshSettings");
}
</script>

<template>
  <div v-if="auth.user">
    <PageTitle>{{ t("auth.settings.security.header") }}</PageTitle>
    <h3 class="text-lg font-bold mb-2">{{ t("auth.settings.security.authApp.name") }}</h3>
    <p class="mb-2">{{ settings?.hasTotp ? t("auth.settings.security.authApp.active") : t("auth.settings.security.authApp.none") }}</p>
    <Button v-if="settings?.hasTotp" :disabled="loading" @click="unlinkTotp">{{ t("auth.settings.security.button.unlinkTotp") }}</Button>
    <Button v-else-if="!totpData" :disabled="loading" @click="setupTotp"> {{ t("auth.settings.security.button.setupAuthApp") }} </Button>
    <div v-else class="flex lt-sm:flex-col gap-8">
      <div class="flex flex-col gap-2 basis-1/2">
        <p>{{ t("auth.settings.security.authAppSetup.scan") }}</p>
        <p>{{ t("auth.settings.security.authAppSetup.cantScan") }}</p>
        <div class="mt-auto flex flex-col gap-2">
          <p>{{ t("auth.settings.security.authAppSetup.enterTotp") }}</p>
          <InputText v-model="totpCode" label="TOTP Code" inputmode="numeric" :rules="[requiredIf()(() => totpData != undefined)]" />
          <Button :disabled="loading || v.$invalid" @click="addTotp"> {{ t("auth.settings.security.authAppSetup.verifyTotp") }} </Button>
        </div>
      </div>
      <div class="basis-1/2">
        <img :src="totpData.qrCode" alt="QR code for TOTP setup" class="w-60" />
        <small>{{ totpData.secret }}</small>
      </div>
    </div>

    <h3 class="text-lg font-bold mt-4 mb-2">{{ t("auth.settings.security.securityKeys.name") }}</h3>
    <ul v-if="settings?.authenticators">
      <li v-for="authenticator in settings.authenticators" :key="authenticator.id" class="my-1">
        {{ authenticator.displayName }} <small class="mr-2">(added at <PrettyTime :time="authenticator.addedAt" long />)</small>
        <Button size="small" :disabled="loading" @click.prevent="unregisterAuthenticator(authenticator)">
          {{ t("auth.settings.security.securityKeys.unregister") }}
        </Button>
        <Button class="ml-2" size="small" :disabled="loading" @click.prevent="renameAuthenticatorModal(authenticator)">
          {{ t("auth.settings.security.securityKeys.rename") }}
        </Button>
      </li>
      <Modal
        ref="authenticatorRenameModal"
        title="Rename authenticator"
        @close="
          authenticatorRenameModal.isOpen = false;
          v.$reset();
        "
      >
        <InputText
          v-model="newAuthenticatorName"
          :label="t('auth.settings.security.securityKeys.keyName')"
          :rules="[requiredIf()(() => authenticatorRenameModal?.isOpen)]"
        />
        <Button class="mt-2" size="small" :disabled="loading" @click.prevent="renameAuthenticator">
          {{ t("auth.settings.security.securityKeys.rename") }}
        </Button>
      </Modal>
    </ul>
    <p v-if="settings?.authenticators.length == 0">{{ t("auth.settings.security.securityKeys.none") }}</p>
    <h4 class="font-semibold mt-4 mb-2">{{ t("auth.settings.security.securityKeys.registerTitle") }}</h4>
    <div class="my-2">
      <InputText
        v-model="authenticatorName"
        :label="t('auth.settings.security.securityKeys.keyName')"
        :rules="[requiredIf()(() => totpData == undefined && !authenticatorRenameModal?.isOpen)]"
      />
    </div>
    <Button :disabled="loading" @click="addAuthenticator"> {{ t("auth.settings.security.button.setupSecurityKey") }} </Button>

    <template v-if="settings?.hasBackupCodes">
      <h3 class="text-lg font-bold mt-4 mb-2">{{ t("auth.settings.security.backupCodes.name") }}</h3>
      <p class="mb-2">{{ t("auth.settings.security.backupCodes.info") }}</p>
      <div v-if="showCodes" class="flex flex-wrap mt-2 mb-2">
        <div v-for="code in codes" :key="code.code" class="basis-3/12">
          <code>{{ code["used_at"] ? t("general.used") : code.code }}</code>
        </div>
      </div>
      <div class="flex gap-2">
        <Button v-if="!showCodes" :disabled="loading" @click="revealCodes"> {{ t("general.reveal") }} </Button>
        <Button :disabled="loading" @click="generateNewCodes"> {{ t("auth.settings.security.backupCodes.generateNew") }} </Button>
      </div>
    </template>

    <h3 class="text-lg font-bold mt-4 mb-2">OAuth</h3>
    <div class="flex gap-2 mt-2">
      <Button v-for="provider in backendData.security.oauthProviders" :key="provider" :disabled="loading" @click="setupOAuth(provider)">
        <template v-if="provider === 'github'">
          <IconMdiGithub class="mr-1" />
          {{ t("auth.settings.security.button.linkGithub") }}
        </template>
        <template v-else-if="provider === 'google'">
          <IconMdiGoogle class="mr-1" />
          {{ t("auth.settings.security.button.linkGoogle") }}
        </template>
        <template v-else-if="provider === 'microsoft'">
          <IconMdiMicrosoft class="mr-1" />
          {{ t("auth.settings.security.button.linkMicrosoft") }}
        </template>
        <template v-else> {{ t("auth.settings.security.button.linkOther", [provider]) }} </template>
      </Button>
    </div>
    <div class="flex gap-2 mt-2">
      <Button
        v-for="credential in settings?.oauthConnections"
        :key="credential.provider + credential.id"
        :disabled="!settings?.hasPassword && settings?.oauthConnections.length === 1"
        :title="!settings?.hasPassword && settings?.oauthConnections.length === 1 ? t('auth.settings.security.unlinkOAuth.cantUnlink') : undefined"
        @click="unlinkOAuth(credential.provider, credential.id)"
      >
        <template v-if="credential.provider === 'github'">
          <IconMdiGithub class="mr-1" />
          {{ t("auth.settings.security.button.unlinkAccount", ["GitHub", credential.name]) }}
        </template>
        <template v-else> {{ t("auth.settings.security.button.unlinkAccount", [credential.provider, credential.name]) }} </template>
      </Button>
    </div>

    <Modal ref="oauthModal" :title="t('auth.settings.security.unlinkOAuth.modal.title')" @close="closeUnlinkModal">
      <p>{{ t("auth.settings.security.unlinkOAuth.modal.message", [currentlyUnlinkingProvider]) }}</p>
      <Link :href="unlinkUrl" target="_blank"> {{ t("auth.settings.security.unlinkOAuth.modal.unlinkUrl", [currentlyUnlinkingProvider]) }} </Link>
    </Modal>

    <Modal ref="backupCodeModal" :title="t('auth.settings.security.backupCodes.modal.title')" @close="backupCodeModal.isOpen = false">
      {{ t("auth.settings.security.backupCodes.modal.needConfigure") }}
      <div class="flex flex-wrap mt-2 mb-2">
        <div v-for="code in codes" :key="code.code" class="basis-3/12">
          <code>{{ code.code }}</code>
        </div>
      </div>
      <p class="mb-2">{{ t("auth.settings.security.backupCodes.modal.confirm") }}</p>
      <InputText
        v-model="backupCodeConfirm"
        :label="t('auth.settings.security.backupCodes.modal.backupCode')"
        :rules="[requiredIf()(backupCodeModal?.isOpen)]"
      />
      <Button class="mt-2" :disabled="v.$invalid" @click="confirmAndRepeat">{{ t("general.confirm") }}</Button>
    </Modal>

    <!-- TODO: implement session list
    <h3 class="text-lg font-bold mt-4 mb-2">{{ t("auth.settings.security.devices") }}</h3>
    <ComingSoon>
      last login<br />
      on revoke iphone<br />
      revoke all
    </ComingSoon>
    -->
  </div>
</template>
