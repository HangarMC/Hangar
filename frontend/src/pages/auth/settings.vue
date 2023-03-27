<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useRoute } from "vue-router";
import { useSeo } from "~/composables/useSeo";
import { useAuthStore } from "~/store/auth";
import Button from "~/lib/components/design/Button.vue";
import { useInternalApi } from "~/composables/useApi";
import { encodeBase64Url, getAttestationOptions } from "~/composables/useWebAuthN";

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
  await useInternalApi("webauthn/register", "POST", {
    name,
    credentialId: credential.id,
    clientData: encodeBase64Url(publicKeyCredential.response.clientDataJSON),
    attestationObject: encodeBase64Url(publicKeyCredential.response.attestationObject),
  });
}

useHead(useSeo("Settings", null, route, null));
</script>

<template>
  <div>
    <h1>Hello {{ auth.user.name }}</h1>
    <Button @click="addAuthenticator">Add authenticator</Button>
  </div>
</template>
