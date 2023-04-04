<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useRoute, useRouter } from "vue-router";
import { reactive, ref } from "vue";
import * as webauthnJson from "@github/webauthn-json";
import { AuthSettings } from "hangar-internal";
import { useSeo } from "~/composables/useSeo";
import { useAuthStore } from "~/store/auth";
import Button from "~/lib/components/design/Button.vue";
import { useInternalApi } from "~/composables/useApi";
import { useAuthSettings } from "~/composables/useApiHelper";
import Card from "~/lib/components/design/Card.vue";
import InputText from "~/lib/components/ui/InputText.vue";
import AvatarChangeModal from "~/lib/components/modals/AvatarChangeModal.vue";
import { definePageMeta } from "#imports";
import Alert from "~/lib/components/design/Alert.vue";
import Modal from "~/lib/components/modals/Modal.vue";
import InputPassword from "~/lib/components/ui/InputPassword.vue";
import { useNotificationStore } from "~/lib/store/notification";
import ComingSoon from "~/lib/components/design/ComingSoon.vue";

definePageMeta({
  loginRequired: true,
});

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const notification = useNotificationStore();

const settings = await useAuthSettings();

const emailConfirmModal = ref();
const emailCodeHasBeenSend = ref(settings.value?.emailPending);
const emailCode = ref();

const loading = ref(false);

if (process.client && route.query.verify) {
  // no await, we dont need to block
  verifyEmail(route.query.verify as string);
}

async function sendEmailCode() {
  loading.value = true;
  try {
    await useInternalApi("auth/email/send", "POST");
    emailCodeHasBeenSend.value = true;
  } catch (e) {
    notification.error(e);
  }
  loading.value = false;
}

async function verifyEmail(emailCode: string) {
  if (!settings.value) return;
  loading.value = true;
  try {
    await useInternalApi("auth/email/verify", "POST", emailCode, { headers: { "content-type": "text/plain" } });
    settings.value!.emailConfirmed = true;
    settings.value!.emailPending = false;
    emailConfirmModal.value.isOpen = false;
    notification.success("Email verified!");
    await router.replace({ query: { verify: undefined } });
  } catch (e) {
    notification.error(e);
  }
  loading.value = false;
}

const authenticatorName = ref<string>();

async function addAuthenticator() {
  loading.value = true;
  try {
    const credentialCreateOptions = await useInternalApi<string>("auth/webauthn/setup", "POST", authenticatorName.value, {
      headers: { "content-type": "text/plain" },
    });
    const parsed = JSON.parse(credentialCreateOptions);
    console.log("credentialCreateOptions", parsed);
    const publicKeyCredential = await webauthnJson.create(parsed);
    console.log("publicKeyCredential", publicKeyCredential);
    await useInternalApi("auth/webauthn/register", "POST", JSON.stringify(publicKeyCredential), { headers: { "content-type": "text/plain" } });
    authenticatorName.value = "";
    settings.value = await useInternalApi<AuthSettings>("auth/settings", "POST");
  } catch (e) {
    notification.error(e);
  }
  loading.value = false;
}

async function unregisterAuthenticator(authenticator: AuthSettings["authenticators"][0]) {
  loading.value = true;
  try {
    await useInternalApi("auth/webauthn/unregister", "POST", authenticator.id, { headers: { "content-type": "text/plain" } });
    settings.value = await useInternalApi<AuthSettings>("auth/settings", "POST");
  } catch (e) {
    notification.error(e);
  }
  loading.value = false;
}

const hasTotp = ref(settings.value?.hasTotp);
const totpData = ref<{ secret: string; qrCode: string } | undefined>();

async function setupTotp() {
  loading.value = true;
  try {
    const data = await useInternalApi<{ secret: string; qrCode: string }>("auth/totp/setup", "POST");
    totpData.value = data;
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

const hasCodes = ref(settings.value?.hasBackupCodes);
const showCodes = ref(false);
const codes = ref();

async function setupCodes() {
  loading.value = true;
  try {
    const data = await useInternalApi("auth/codes/setup", "POST");
    console.log("data", data);
    codes.value = data;
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
      const data = await useInternalApi("auth/codes/show", "POST");
      codes.value = data;
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
    const data = await useInternalApi("auth/codes/regenerate", "POST");
    hasCodes.value = false;
    codes.value = data;
  } catch (e) {
    notification.error(e);
  }
  loading.value = false;
}

const accountForm = reactive({
  username: auth.user?.name,
  email: auth.user?.email,
  currentPassword: "",
  newPassword: "",
});

function saveAccount() {
  loading.value = true;
  try {
    // todo saveAccount
  } catch (e) {
    notification.error(e);
  }
  loading.value = false;
}

const profileForm = reactive({
  tagline: auth.user?.tagline,
  discord: auth.user?.socials?.discord,
  github: auth.user?.socials?.github,
});

function saveProfile() {
  loading.value = true;
  try {
    // todo saveProfile
  } catch (e) {
    notification.error(e);
  }
  loading.value = false;
}

useHead(useSeo("Settings", null, route, null));
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
    <!-- todo tabs -->

    <Alert v-if="settings?.emailPending" class="col-span-1 md:col-span-2">
      Got your email confirmation? Enter the code
      <Button size="small" :disabled="loading" @click="emailConfirmModal.isOpen = true">here</Button>
      !
    </Alert>
    <Alert v-else-if="!settings?.emailConfirmed" class="col-span-1 md:col-span-2">
      You haven't verified your email yet, click
      <Button size="small" :disabled="loading" @click="emailConfirmModal.isOpen = true">here</Button>
      to change that!
    </Alert>

    <Modal ref="emailConfirmModal" title="Confirm email" @close="emailConfirmModal.isOpen = false">
      <template v-if="!emailCodeHasBeenSend">
        <p class="mb-2">We will send you a code via email</p>
        <Button :disabled="loading" @click="sendEmailCode">Send</Button>
      </template>
      <div v-else class="flex flex-col gap-2">
        <p>Enter the code you received via email here</p>
        <InputText v-model="emailCode" label="Code" />
        <Button class="w-max" :disabled="loading" @click="verifyEmail(emailCode)">Verify Code</Button>
      </div>
    </Modal>

    <Card>
      <template #header>Manage Account</template>
      <form class="flex flex-col gap-2">
        <InputText v-model="accountForm.username" label="username" />
        <InputText v-model="accountForm.email" label="email" />
        (status: {{ settings?.emailConfirmed ? "confirmed" : "unconfirmed!" }})
        <Button v-if="!settings?.emailConfirmed" class="w-max" size="small" :disabled="loading" @click.prevent="emailConfirmModal.isOpen = true">
          Confirm
        </Button>
        <InputPassword v-model="accountForm.currentPassword" label="current-password" name="current-password" autofill="current-password" />
        <InputPassword v-model="accountForm.newPassword" label="new-password (optional)" name="new-password" autofill="new-password" />
        <Button type="submit" class="w-max" :disabled="loading" @click.prevent="saveAccount">Save</Button>
      </form>
    </Card>

    <Card>
      <template #header>Manage Profile</template>
      <form class="flex flex-col gap-2">
        <InputText v-model="profileForm.tagline" label="tagline" />
        <!-- todo make avatar change nicer, use the old stuff from old settings maybe? -->
        <AvatarChangeModal class="w-max" :avatar="auth.user?.avatarUrl!" :action="`users/${auth.user?.name}/settings/avatar`" />
        <InputText v-model="profileForm.discord" label="discord" />
        <InputText v-model="profileForm.github" label="github" />
        <Button type="submit" class="w-max" :disabled="loading" @click.prevent="saveProfile">Save</Button>
      </form>
    </Card>

    <Card>
      <template #header>Manage TOTP</template>
      <Button v-if="hasTotp" :disabled="loading" @click="unlinkTotp">Unlink totp</Button>
      <Button v-else-if="!totpData" :disabled="loading" @click="setupTotp">Setup totp</Button>
      <div v-else class="flex gap-2">
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
    </Card>

    <Card>
      <template #header>Manage Security Keys (WebAuthN)</template>
      <div>Authenticators:</div>
      <ul v-if="settings?.authenticators">
        <li v-for="authenticator in settings.authenticators" :key="authenticator.id" class="my-1">
          {{ authenticator.displayName }} <small class="mr-2">(added at {{ authenticator.addedAt }})</small>
          <Button size="small" :disabled="loading" @click.prevent="unregisterAuthenticator(authenticator)">Unregister</Button>
        </li>
      </ul>
      <div class="my-2">
        <InputText v-model="authenticatorName" label="Name" />
      </div>
      <Button :disabled="loading" @click="addAuthenticator">Add authenticator</Button>
    </Card>

    <Card>
      <template #header>Manage Backup Codes</template>
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
    </Card>

    <Card>
      <template #header>Manage Devices</template>
      <ComingSoon>
        last login<br />
        on revoke iphone<br />
        revoke all
      </ComingSoon>
    </Card>
  </div>
</template>
