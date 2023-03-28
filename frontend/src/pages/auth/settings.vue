<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useRoute } from "vue-router";
import { ref } from "vue";
import { useSeo } from "~/composables/useSeo";
import { useAuthStore } from "~/store/auth";
import Button from "~/lib/components/design/Button.vue";
import { useInternalApi } from "~/composables/useApi";
import { encodeBase64Url, getAttestationOptions } from "~/composables/useWebAuthN";
import Card from "~/lib/components/design/Card.vue";
import InputText from "~/lib/components/ui/InputText.vue";
import AvatarChangeModal from "~/lib/components/modals/AvatarChangeModal.vue";

const route = useRoute();

const auth = useAuthStore();

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

const hasTotp = ref(true); // TODO implement getting aal methods
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

function saveAccount() {
  //
}

function saveProfile() {
  //
}

useHead(useSeo("Settings", null, route, null));
</script>

<template>
  <div>
    <h1>Hello {{ auth.user.name }}</h1>
    <!-- todo tabs -->

    <Card>
      <template #header>account</template>
      <form>
        <InputText label="username" />
        <InputText label="email" /> (status: confirmed)
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
        <AvatarChangeModal />
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
      <Button>Add</Button>
      <Button>Reveal</Button>
      <Button>Revoke</Button>
    </Card>

    <Card>
      <template #header>devices</template>
      last login on
      <Button>revoke iphone</Button>
      <Button>revoke all</Button>
    </Card>
  </div>
</template>
