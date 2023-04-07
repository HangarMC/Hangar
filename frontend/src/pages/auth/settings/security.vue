<script lang="ts" setup>
import { ref } from "vue";
import * as webauthnJson from "@github/webauthn-json";
import { AuthSettings } from "hangar-internal";
import { useI18n } from "vue-i18n";
import { useVuelidate } from "@vuelidate/core";
import { useAuthStore } from "~/store/auth";
import { useNotificationStore } from "~/store/notification";
import { useInternalApi } from "~/composables/useApi";
import ComingSoon from "~/components/design/ComingSoon.vue";
import Button from "~/components/design/Button.vue";
import InputText from "~/components/ui/InputText.vue";
import { definePageMeta } from "#imports";

definePageMeta({
  globalPermsRequired: ["EDIT_OWN_USER_SETTINGS"],
});

const props = defineProps<{
  settings?: AuthSettings;
}>();
const emit = defineEmits<{
  refreshSettings: () => void;
}>();

const auth = useAuthStore();
const notification = useNotificationStore();
const { t } = useI18n();
const v = useVuelidate();

const loading = ref(false);

const authenticatorName = ref<string>();

async function addAuthenticator() {
  loading.value = true;
  try {
    const credentialCreateOptions = await useInternalApi<string>("auth/webauthn/setup", "POST", authenticatorName.value, {
      headers: { "content-type": "text/plain" },
    });
    const parsed = JSON.parse(credentialCreateOptions);
    const publicKeyCredential = await webauthnJson.create(parsed);
    await useInternalApi("auth/webauthn/register", "POST", JSON.stringify(publicKeyCredential), { headers: { "content-type": "text/plain" } });
    authenticatorName.value = "";
    emit.refreshSettings();
  } catch (e) {
    notification.error(e);
  }
  loading.value = false;
}

async function unregisterAuthenticator(authenticator: AuthSettings["authenticators"][0]) {
  loading.value = true;
  try {
    await useInternalApi("auth/webauthn/unregister", "POST", authenticator.id, { headers: { "content-type": "text/plain" } });
    emit.refreshSettings();
  } catch (e) {
    notification.error(e);
  }
  loading.value = false;
}

const hasTotp = ref(props.settings?.hasTotp);
const totpData = ref<{ secret: string; qrCode: string } | undefined>();

async function setupTotp() {
  loading.value = true;
  try {
    totpData.value = await useInternalApi<{ secret: string; qrCode: string }>("auth/totp/setup", "POST");
  } catch (e) {
    notification.error(e);
  }
  loading.value = false;
}

const totpCode = ref();

async function addTotp() {
  loading.value = true;
  try {
    await useInternalApi("auth/totp/register", "POST", { secret: totpData.value?.secret, code: totpCode.value });
    hasTotp.value = true;
  } catch (e) {
    notification.error(e);
  }
  loading.value = false;
}

async function unlinkTotp() {
  loading.value = true;
  try {
    await useInternalApi("auth/totp/remove", "POST");
    hasTotp.value = false;
  } catch (e) {
    notification.error(e);
  }
  loading.value = false;
}

const hasCodes = ref(props.settings?.hasBackupCodes);
const showCodes = ref(false);
const codes = ref();

async function setupCodes() {
  loading.value = true;
  try {
    codes.value = await useInternalApi("auth/codes/setup", "POST");
  } catch (e) {
    notification.error(e);
  }
  loading.value = false;
}

async function confirmCodes() {
  loading.value = true;
  try {
    await useInternalApi("auth/codes/register", "POST");
    hasCodes.value = true;
    showCodes.value = false;
  } catch (e) {
    notification.error(e);
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
    notification.error(e);
  }
  loading.value = false;
}

async function generateNewCodes() {
  loading.value = true;
  try {
    codes.value = await useInternalApi("auth/codes/regenerate", "POST");
    hasCodes.value = false;
  } catch (e) {
    notification.error(e);
  }
  loading.value = false;
}
</script>

<template>
  <div v-if="auth.user">
    <h2 class="text-xl font-bold mb-4">{{ t("auth.settings.security.header") }}</h2>
    <h3 class="text-lg font-bold mb-2">Authenticator App</h3>
    <Button v-if="hasTotp" :disabled="loading" @click="unlinkTotp">Unlink totp</Button>
    <Button v-else-if="!totpData" :disabled="loading" @click="setupTotp">Setup 2FA via authenticator app</Button>
    <div v-else class="flex lt-sm:flex-col gap-8">
      <div class="flex flex-col gap-2 basis-1/2">
        <p>Scan the code on the right using your favorite authenticator app</p>
        <p>Can't scan? enter the code listed below the image!</p>
        <InputText v-model="totpCode" label="Code" inputmode="numeric" />
        <Button :disabled="loading" @click="addTotp">Verify code and activate</Button>
      </div>
      <div class="basis-1/2">
        <img :src="totpData.qrCode" alt="totp qr code" class="w-60" />
        <small>{{ totpData.secret }}</small>
      </div>
    </div>

    <h3 class="text-lg font-bold mt-6 mb-2">Security Keys</h3>
    <ul v-if="settings?.authenticators">
      <li v-for="authenticator in settings.authenticators" :key="authenticator.id" class="my-1">
        {{ authenticator.displayName }} <small class="mr-2">(added at {{ authenticator.addedAt }})</small>
        <Button size="small" :disabled="loading" @click.prevent="unregisterAuthenticator(authenticator)">Unregister</Button>
      </li>
    </ul>
    <div class="my-2">
      <InputText v-model="authenticatorName" label="Name" />
    </div>
    <Button :disabled="loading" @click="addAuthenticator">Setup 2FA via security key</Button>

    <h3 class="text-lg font-bold mt-6 mb-2">Backup Codes</h3>
    <div v-if="(hasCodes && showCodes) || (!hasCodes && codes)" class="flex flex-wrap mt-2 mb-2">
      <div v-for="code in codes" :key="code.code" class="basis-3/12">
        <code>{{ code["used_at"] ? "Used" : code.code }}</code>
      </div>
    </div>
    <div v-if="hasCodes" class="flex gap-2">
      <Button v-if="!showCodes" :disabled="loading" @click="revealCodes">Reveal</Button>
      <Button :disabled="loading" @click="generateNewCodes">Generate new codes</Button>
    </div>
    <Button v-else-if="!codes" :disabled="loading" @click="setupCodes">Add</Button>
    <Button v-else :disabled="loading" @click="confirmCodes">Confirm codes</Button>

    <h3 class="text-lg font-bold mt-6 mb-2">Devices</h3>
    <ComingSoon>
      last login<br />
      on revoke iphone<br />
      revoke all
    </ComingSoon>
  </div>
</template>
