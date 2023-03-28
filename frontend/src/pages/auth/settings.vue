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

const totpData = ref<{ secret: string; qrCode: string } | undefined>();
async function setupTotp() {
  const data = await useInternalApi<{ secret: string; qrCode: string }>("auth/totp/setup");
  console.log("data", data);
  totpData.value = data;
}

useHead(useSeo("Settings", null, route, null));
</script>

<template>
  <div>
    <h1>Hello {{ auth.user.name }}</h1>
    <Button @click="addAuthenticator">Add authenticator</Button>

    <Card>
      <template #header>Totp</template>
      <Button v-if="!totpData" @click="setupTotp">Setup totp</Button>
      <template v-else>
        <img :src="totpData.qrCode" />
        <small>{{ totpData.secret }}</small>
      </template>
    </Card>
  </div>
</template>
