<script lang="ts" setup>
import { ref } from "vue";
import * as webauthnJson from "@github/webauthn-json";
import type { AuthSettings } from "hangar-internal";
import { useI18n } from "vue-i18n";
import { useVuelidate } from "@vuelidate/core";
import type { AxiosRequestConfig } from "axios";
import { useAuthStore } from "~/store/auth";
import { useNotificationStore } from "~/store/notification";
import { useInternalApi } from "~/composables/useApi";
import ComingSoon from "~/components/design/ComingSoon.vue";
import Button from "~/components/design/Button.vue";
import InputText from "~/components/ui/InputText.vue";
import { requiredIf, useAuth, useAxios, useRoute, useRouter } from "#imports";
import PageTitle from "~/components/design/PageTitle.vue";
import Modal from "~/components/modals/Modal.vue";
import PrettyTime from "~/components/design/PrettyTime.vue";

const props = defineProps<{
  settings?: AuthSettings;
}>();
const emit = defineEmits<{
  refreshSettings: () => void;
}>();

const auth = useAuthStore();
const notification = useNotificationStore();
const i18n = useI18n();
const { t } = i18n;
const v = useVuelidate();
const router = useRouter();
const route = useRoute();

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
    if (e.response?.status === 499) {
      codes.value = e.response.data.body;
      backupCodeModal.value.isOpen = true;
      savedRequest.value = e.config;
    } else if (e.response?.data?.detail === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else if (e?.toString()?.startsWith("NotAllowedError")) {
      notification.error("Security Key Authentication failed!");
    } else {
      notification.fromError(i18n, e);
    }
  }
  loading.value = false;
}

async function unregisterAuthenticator(authenticator: AuthSettings["authenticators"][0]) {
  loading.value = true;
  try {
    await useInternalApi("auth/webauthn/unregister", "POST", authenticator.id, { headers: { "content-type": "text/plain" } });
    emit("refreshSettings");
  } catch (e) {
    if (e.response?.data?.detail === "error.privileged") {
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
    if (e.response?.data?.detail === "error.privileged") {
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
    if (e.response.status === 499) {
      codes.value = e.response.data.body;
      backupCodeModal.value.isOpen = true;
      savedRequest.value = e.config;
      otp.value = e.response.headers["x-hangar-verify"];
    } else if (e.response?.data?.detail === "error.privileged") {
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
    if (e.response?.data?.detail === "error.privileged") {
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
    if (e.response?.data?.detail === "error.privileged") {
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
    if (e.response?.data?.detail === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else {
      notification.fromError(i18n, e);
    }
  }
  loading.value = false;
}
</script>

<template>
  <div v-if="auth.user">
    <PageTitle>{{ t("auth.settings.security.header") }}</PageTitle>
    <h3 class="text-lg font-bold mb-2">Authenticator App</h3>
    <Button v-if="settings?.hasTotp" :disabled="loading" @click="unlinkTotp">Unlink totp</Button>
    <Button v-else-if="!totpData" :disabled="loading" @click="setupTotp">Setup 2FA via authenticator app</Button>
    <div v-else class="flex lt-sm:flex-col gap-8">
      <div class="flex flex-col gap-2 basis-1/2">
        <p>Scan the QR code on the right using your favorite authenticator app</p>
        <p>Can't scan? Enter the secret listed below the image!</p>
        <div class="mt-auto flex flex-col gap-2">
          <p>Enter a TOTP code generated by your authenticator app in the box below.</p>
          <InputText v-model="totpCode" label="TOTP Code" inputmode="numeric" :rules="[requiredIf()(() => totpData != undefined)]" />
          <Button :disabled="loading || v.$invalid" @click="addTotp">Verify TOTP code and activate</Button>
        </div>
      </div>
      <div class="basis-1/2">
        <img :src="totpData.qrCode" alt="totp qr code" class="w-60" />
        <small>{{ totpData.secret }}</small>
      </div>
    </div>

    <h3 class="text-lg font-bold mt-4 mb-2">Security Keys</h3>
    <ul v-if="settings?.authenticators">
      <li v-for="authenticator in settings.authenticators" :key="authenticator.id" class="my-1">
        {{ authenticator.displayName }} <small class="mr-2">(added at <PrettyTime :time="authenticator.addedAt" long />)</small>
        <Button size="small" :disabled="loading" @click.prevent="unregisterAuthenticator(authenticator)">Unregister</Button>
      </li>
    </ul>
    <div class="my-2">
      <InputText v-model="authenticatorName" label="Name" :rules="[requiredIf()(() => totpData == undefined)]" />
    </div>
    <Button :disabled="loading" @click="addAuthenticator">Setup 2FA via security key</Button>

    <template v-if="settings?.hasBackupCodes">
      <h3 class="text-lg font-bold mt-4 mb-2">Backup Codes</h3>
      <div v-if="showCodes" class="flex flex-wrap mt-2 mb-2">
        <div v-for="code in codes" :key="code.code" class="basis-3/12">
          <code>{{ code["used_at"] ? "Used" : code.code }}</code>
        </div>
      </div>
      <div class="flex gap-2">
        <Button v-if="!showCodes" :disabled="loading" @click="revealCodes">Reveal</Button>
        <Button :disabled="loading" @click="generateNewCodes">Generate new codes</Button>
      </div>
    </template>

    <Modal ref="backupCodeModal" title="Confirm backup codes" @close="backupCodeModal.isOpen = false">
      You need to configure backup codes before you can activate 2fa. Please save these codes securely!
      <div class="flex flex-wrap mt-2 mb-2">
        <div v-for="code in codes" :key="code.code" class="basis-3/12">
          <code>{{ code.code }}</code>
        </div>
      </div>
      Confirm that you saved the backup codes by entering one of them below
      <InputText v-model="backupCodeConfirm" label="Backup Code" :rules="[requiredIf()(backupCodeModal.isOpen)]" />
      <Button class="mt-2" :disabled="v.$invalid" @click="confirmAndRepeat">Confirm</Button>
    </Modal>

    <h3 class="text-lg font-bold mt-4 mb-2">Devices</h3>
    <ComingSoon>
      last login<br />
      on revoke iphone<br />
      revoke all
    </ComingSoon>
  </div>
</template>
