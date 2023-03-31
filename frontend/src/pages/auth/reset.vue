<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useRoute, useRouter } from "vue-router";
import { ref } from "vue";
import { useVuelidate } from "@vuelidate/core";
import { useSeo } from "~/composables/useSeo";
import Card from "~/lib/components/design/Card.vue";
import InputText from "~/lib/components/ui/InputText.vue";
import Button from "~/lib/components/design/Button.vue";
import { email as emailRule, required } from "~/lib/composables/useValidationHelpers";
import { useInternalApi } from "~/composables/useApi";

const route = useRoute();
const router = useRouter();
const v = useVuelidate();

const email = ref<string>();
const code = ref<string>();
const password = ref<string>();
const codeSend = ref(false);
const codeError = ref<string[]>();
const codeVerified = ref(false);
const passwordUpdated = ref(false);

async function sendCode() {
  if (!(await v.value.$validate())) return;
  await useInternalApi("auth/reset/send", "POST", { email: email.value });
  codeSend.value = true;
}

async function verifyCode() {
  if (!(await v.value.$validate())) return;
  codeError.value = [];
  try {
    await useInternalApi("auth/reset/verify", "POST", { email: email.value, code: code.value });
    codeVerified.value = true;
  } catch (e) {
    codeError.value = e.response && e.response.status === 400 ? ["Invalid code"] : ["Unknown error"];
  }
}

async function sendNewPassword() {
  if (!(await v.value.$validate())) return;
  await useInternalApi("auth/reset/set", "POST", { email: email.value, code: code.value, password: password.value });
  passwordUpdated.value = true;
}

async function gotoLogin() {
  await router.push("/auth/login");
}

useHead(useSeo("Reset your password", null, route, null));
</script>

<template>
  <Card>
    <template #header> Reset your password </template>
    <form v-if="!codeSend">
      <p>Enter your email here to receive a verification code to your email address</p>
      <InputText v-model="email" type="email" label="email" name="email" autocomplete="email" :rules="[required(), emailRule()]" />
      <Button @click.prevent="sendCode">Send code</Button>
    </form>
    <form v-else-if="!codeVerified">
      <p>Enter the code you got via email here</p>
      <InputText v-model="code" type="text" inputmode="numeric" pattern="[0-9]" label="code" :rules="[required()]" :error-messages="codeError" />
      <Button @click.prevent="verifyCode">Verify Code</Button>
    </form>
    <form v-else-if="!passwordUpdated">
      <p>Enter your new password</p>
      <input id="email" v-model="email" type="hidden" name="email" />
      <!-- todo password -->
      <InputText v-model="password" type="password" label="new password" name="new-password" autocomplete="new-password" />
      <Button @click.prevent="sendNewPassword">Update password</Button>
    </form>
    <template v-else>
      <p>Password updated!</p>
      <Button @click="gotoLogin">Go to Login</Button>
    </template>
  </Card>
</template>
