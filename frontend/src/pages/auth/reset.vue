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
import InputPassword from "~/lib/components/ui/InputPassword.vue";

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
    <form v-if="!codeSend" class="flex flex-col gap-2">
      <p>Enter your email here to receive a verification code to your email address</p>
      <InputText v-model="email" type="email" label="email" name="email" autocomplete="email" :rules="[required(), emailRule()]" />
      <Button class="w-max" @click.prevent="sendCode">Send code</Button>
    </form>
    <form v-else-if="!codeVerified" class="flex flex-col gap-2">
      <p>Enter the code you got via email here</p>
      <InputText v-model="code" type="text" inputmode="numeric" pattern="[0-9]" label="code" :rules="[required()]" :error-messages="codeError" />
      <Button class="w-max" @click.prevent="verifyCode">Verify Code</Button>
    </form>
    <form v-else-if="!passwordUpdated" class="flex flex-col gap-2">
      <p>Enter your new password</p>
      <input id="email" v-model="email" type="hidden" name="email" />
      <InputPassword v-model="password" label="new password" name="new-password" autocomplete="new-password" :rules="[required()]" />
      <Button class="w-max" @click.prevent="sendNewPassword">Update password</Button>
    </form>
    <div v-else class="flex flex-col gap-2">
      <p>Password updated!</p>
      <Button class="w-max" @click="gotoLogin">Go to Login</Button>
    </div>
  </Card>
</template>
