<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useRoute, useRouter } from "vue-router";
import { ref } from "vue";
import { useSeo } from "~/composables/useSeo";
import InputText from "~/lib/components/ui/InputText.vue";
import Button from "~/lib/components/design/Button.vue";
import { useAxios } from "~/composables/useAxios";
import { useAuth } from "~/composables/useAuth";
import { useInternalApi } from "~/composables/useApi";
import { encodeBase64Url, getAssertionOptions } from "~/composables/useWebAuthN";

const route = useRoute();
const router = useRouter();

const aal = ref(2);
// aal1
const username = ref("");
const password = ref("");

// aal2

async function loginPassword() {
  const result = await useAxios()
    .post("/api/internal/auth/login", new URLSearchParams({ username: username.value, password: password.value }), {
      headers: { "content-type": "application/x-www-form-urlencoded" },
    })
    .catch((r) => {
      console.log("dum", r);
      return r;
    });

  if (result.status === 200) {
    console.log("login success!");
    await useAuth.updateUser();
    // TODO redirect to where you started
    await router.push("/auth/settings");
  } else {
    console.log("login fail", result);
  }
}

async function loginWebAuthN() {
  const options = await getAssertionOptions();
  const credential = await navigator.credentials.get({ publicKey: options });
  if (!credential) {
    console.log("no credential");
    return;
  }
  if (credential.type !== "public-key") {
    console.log("Unexpected credential type");
    return;
  }
  const publicKeyCredential = credential as PublicKeyCredential;
  const assertionResponse = publicKeyCredential.response as AuthenticatorAssertionResponse;
  console.log("assertion", publicKeyCredential);
  const result = await useInternalApi(
    "auth/login",
    "POST",
    new URLSearchParams({
      credentialId: encodeBase64Url(new Uint8Array(publicKeyCredential.rawId)),
      clientDataJSON: encodeBase64Url(new Uint8Array(assertionResponse.clientDataJSON)),
      authenticatorData: encodeBase64Url(new Uint8Array(assertionResponse.authenticatorData)),
      signature: encodeBase64Url(new Uint8Array(assertionResponse.signature)),
      clientExtensionsJSON: JSON.stringify(publicKeyCredential.getClientExtensionResults()),
    }),
    {
      headers: { "content-type": "application/x-www-form-urlencoded" },
    }
  ).catch((r) => {
    console.log("dum", r);
    return r;
  });
  console.log("result", result);
}

useHead(useSeo("Login", null, route, null));
</script>

<template>
  <div>
    <div v-if="aal === 1">
      <InputText v-model="username" label="Username" name="useranme" />
      <!-- todo copy InputPassword from auth -->
      <InputText v-model="password" label="Password" name="password" type="password" />
      <Button @click="loginPassword">Login</Button>
      <Button to="/auth/signup">Signup</Button>
      <Button to="/auth/reset">Forgot</Button>
    </div>

    <div v-if="aal === 2">
      <Button @click="loginWebAuthN">Use WebAuthN</Button>
    </div>
  </div>
</template>
