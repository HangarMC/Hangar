<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useRoute, useRouter } from "vue-router";
import { ref } from "vue";
import { useSeo } from "~/composables/useSeo";
import { useAuthStore } from "~/store/auth";
import Button from "~/lib/components/design/Button.vue";
import { useInternalApi } from "~/composables/useApi";
import { useAuthSettings } from "~/composables/useApiHelper";
import { encodeBase64Url, getAttestationOptions } from "~/composables/useWebAuthN";
import Card from "~/lib/components/design/Card.vue";
import InputText from "~/lib/components/ui/InputText.vue";
import AvatarChangeModal from "~/lib/components/modals/AvatarChangeModal.vue";
import { definePageMeta } from "#imports";
import Alert from "~/lib/components/design/Alert.vue";
import Modal from "~/lib/components/modals/Modal.vue";

definePageMeta({
  loginRequired: true,
});

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const settings = await useAuthSettings();

const emailConfirmModal = ref();
const emailCodeHasBeenSend = ref(settings.value?.emailPending);
const emailCode = ref();

async function sendEmailCode() {
  await useInternalApi("auth/email/send", "POST");
  emailCodeHasBeenSend.value = true;
}

async function verifyEmail() {
  if (!settings.value) return;
  await useInternalApi("auth/email/verify", "POST", emailCode.value, { headers: { "content-type": "text/plain" } });
  settings.value!.emailConfirmed = true;
  settings.value!.emailPending = false;
  emailConfirmModal.value.isOpen = false;
}

async function addAuthenticator() {
  const options = await getAttestationOptions();
  const credential = await navigator.credentials.create({ publicKey: options });
  if (!credential) {
    console.log("no credential");
    return;
  }
  if (credential.type !== "public-key") {
    console.log("Unexpected credential type");
    return;
  }
  const publicKeyCredential = credential as PublicKeyCredential;
  console.log("credential", credential);
  const name = "DummyAuth";
  await useInternalApi("auth/webauthn/register", "POST", {
    name,
    credentialId: credential.id,
    clientData: encodeBase64Url(publicKeyCredential.response.clientDataJSON),
    attestationObject: encodeBase64Url(publicKeyCredential.response.attestationObject),
  });
}

const hasTotp = ref(settings.value?.hasTotp);
const totpData = ref<{ secret: string; qrCode: string } | undefined>();
async function setupTotp() {
  // TODO loading
  const data = await useInternalApi<{ secret: string; qrCode: string }>("auth/totp/setup");
  console.log("data", data);
  // TODO error handling
  totpData.value = data;
}
const totpCode = ref();
async function addTotp() {
  // TODO loading
  await useInternalApi("auth/totp/register", "POST", { secret: totpData.value?.secret, code: totpCode.value });
  // TODO error handling
  hasTotp.value = true;
}

async function unlinkTotp() {
  // TODO loading
  await useInternalApi("auth/totp/remove", "POST");
  // TODO error handling
  hasTotp.value = false;
}

const hasCodes = ref(settings.value?.hasBackupCodes);
const showCodes = ref(false);
const codes = ref();
async function setupCodes() {
  const data = await useInternalApi("auth/codes/setup", "POST");
  console.log("data", data);
  codes.value = data;
}

async function confirmCodes() {
  await useInternalApi("auth/codes/register", "POST");
  hasCodes.value = true;
  showCodes.value = false;
}

async function revealCodes() {
  if (!codes.value) {
    const data = await useInternalApi("auth/codes/show", "POST");
    codes.value = data;
  }
  showCodes.value = true;
}

async function generateNewCodes() {
  const data = await useInternalApi("auth/codes/regenerate", "POST");
  hasCodes.value = false;
  codes.value = data;
}

function saveAccount() {
  // todo saveAccount
}

function saveProfile() {
  // todo saveProfile
}

useHead(useSeo("Settings", null, route, null));
</script>

<template>
  <div>
    <h1>Hello {{ auth.user.name }}</h1>
    <!-- todo tabs -->

    <Alert v-if="settings?.emailPending">
      Got your email confirmation? Enter the code <Button size="small" @click="emailConfirmModal.isOpen = true">here</Button>!
    </Alert>
    <Alert v-else-if="!settings?.emailConfirmed">
      You haven't verified your email yet, click <Button size="small" @click="emailConfirmModal.isOpen = true">here</Button> to change that!
    </Alert>

    <Modal ref="emailConfirmModal" title="Confirm email" @close="emailConfirmModal.isOpen = false">
      <template v-if="!emailCodeHasBeenSend">
        <p>We will send you a code via email</p>
        <Button @click="sendEmailCode">Send</Button>
      </template>
      <template v-else>
        <p>Enter the code you received via email here</p>
        <InputText v-model="emailCode" label="Code" />
        <Button @click="verifyEmail">Verify Code</Button>
      </template>
    </Modal>

    <Card>
      <template #header>account</template>
      <form>
        <InputText label="username" />
        <InputText label="email" /> (status: {{ settings?.emailConfirmed ? "confirmed" : "unconfirmed!" }})
        <Button v-if="!settings?.emailConfirmed" size="small" @click.prevent="emailConfirmModal.isOpen = true"> Confirm </Button>
        <!-- todo port password -->
        <InputText type="password" label="current-password" name="current-password" autofill="current-password" />
        <InputText type="password" label="new-password (optional)" name="new-password" autofill="new-password" />
        <Button type="submit" @click.prevent="saveAccount">Save</Button>
      </form>
    </Card>

    <Card>
      <template #profile>profile</template>
      <form>
        <InputText label="tagline" />
        <AvatarChangeModal :avatar="auth.user?.avatarUrl!" :action="`users/${auth.user?.name}/settings/avatar`" />
        <InputText label="discord" />
        <InputText label="github" />
        <Button type="submit" @click.prevent="saveProfile">Save</Button>
      </form>
    </Card>

    <Card>
      <template #header>Totp</template>
      <Button v-if="hasTotp" @click="unlinkTotp">Unlink totp</Button>
      <Button v-else-if="!totpData" @click="setupTotp">Setup totp</Button>
      <template v-else>
        <img :src="totpData.qrCode" alt="totp qr code" />
        <small>{{ totpData.secret }}</small>
        <InputText v-model="totpCode" label="Code" />
        <Button @click="addTotp">Verify code and activate</Button>
      </template>
    </Card>

    <Card>
      <template #header>webauthn</template>
      <div>authenticators</div>
      <InputText label="Name" />
      <Button @click="addAuthenticator">Add authenticator</Button>
    </Card>

    <Card>
      <template #header>backup codes</template>
      <div>codes</div>
      <div v-if="(hasCodes && showCodes) || (!hasCodes && codes)" class="flex flex-wrap mt-2 mb-2">
        <div v-for="code in codes" :key="code.code" class="basis-3/12">
          <code>{{ code["used_at"] ? "Used" : code.code }}</code>
        </div>
      </div>
      <template v-if="hasCodes">
        <Button v-if="!showCodes" @click="revealCodes">Reveal</Button>
        <Button @click="generateNewCodes">Generate new codes</Button>
      </template>
      <Button v-else-if="!codes" @click="setupCodes">Add</Button>
      <Button v-else @click="confirmCodes">Confirm codes</Button>
    </Card>

    <Card>
      <template #header>devices</template>
      last login on
      <Button>revoke iphone</Button>
      <Button>revoke all</Button>
    </Card>
  </div>
</template>
