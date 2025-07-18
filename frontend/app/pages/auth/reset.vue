<script lang="ts" setup>
import { isAxiosError } from "axios";
import { email as emailRule } from "~/composables/useValidationHelpers";

const route = useRoute("auth-reset");
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
  } catch (err) {
    codeError.value = isAxiosError(err) && err.response?.status === 400 ? ["Invalid code"] : ["Unknown error"];
  }
}

async function sendNewPassword() {
  if (!(await v.value.$validate())) return;
  try {
    await useInternalApi("auth/reset/set", "POST", { email: email.value, code: code.value, password: password.value });
    passwordUpdated.value = true;
  } catch (err) {
    handleRequestError(err);
  }
}

async function gotoLogin() {
  await router.push("/auth/login");
}

useSeo(computed(() => ({ title: "Reset your password", route })));
</script>

<template>
  <Card class="w-xl mx-auto max-w-full">
    <template #header> Reset your password </template>
    <form v-if="!codeSend" class="flex flex-col gap-2">
      <p>Enter your email address here to receive a verification code to reset your password.</p>
      <InputText v-model="email" type="email" label="Email" name="email" autocomplete="email" :rules="[required(), emailRule()]" />
      <Button class="w-max" @click.prevent="sendCode">Send code</Button>
    </form>
    <form v-else-if="!codeVerified" class="flex flex-col gap-2">
      <p>Please enter the code you received via email</p>
      <InputText v-model="code" type="text" inputmode="numeric" pattern="[0-9]" label="Verification code" :rules="[required()]" :error-messages="codeError" />
      <Button class="w-max" @click.prevent="verifyCode">Verify Code</Button>
    </form>
    <form v-else-if="!passwordUpdated" class="flex flex-col gap-2">
      <p>Enter your new password</p>
      <input id="email" v-model="email" type="hidden" name="email" />
      <InputPassword v-model="password" label="New password" name="new-password" autocomplete="new-password" :rules="[required()]" />
      <Button class="w-max" @click.prevent="sendNewPassword">Update password</Button>
    </form>
    <div v-else class="flex flex-col gap-2">
      <p>Password updated!</p>
      <Button class="w-max" @click="gotoLogin">Go to Login</Button>
    </div>
  </Card>
</template>
