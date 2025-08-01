<script lang="ts" setup>
import { jwtDecode } from "jwt-decode";

const route = useRoute("auth-oauth-signup");
const v = useVuelidate();

interface OAuthSignupForm {
  username?: string;
  email?: string;
  jwt?: string;
  tos?: boolean;
}

const done = ref(false);

const notification = useNotificationStore();
const i18n = useI18n();
const form = reactive<OAuthSignupForm>({});
const loading = ref(false);

const errorMessage = ref<string | undefined>();
const emailVerificationNeeded = ref(false);

const data = computed(() =>
  jwtDecode<{
    email: string;
    username: string;
    provider: string;
    returnUrl: string;
  }>(route.query.state as string)
);

form.jwt = route.query.state as string;
form.email = data.value.email;
form.username = data.value.username;
form.tos = false;

async function submit() {
  if (!(await v.value.$validate())) return;
  loading.value = true;
  errorMessage.value = undefined;
  try {
    const result = await useInternalApi<{ emailVerificationNeeded: boolean }>("oauth/register", "POST", form);
    emailVerificationNeeded.value = result.emailVerificationNeeded;
    done.value = true;
  } catch (err) {
    notification.fromError(i18n, err);
  }
  loading.value = false;
}

useSeo(computed(() => ({ title: "OAuth Signup", route })));
</script>

<template>
  <Card class="w-xl mx-auto max-w-full">
    <template #header>
      <h1>OAuth Sign up</h1>
    </template>

    <form v-if="!done" class="flex flex-col gap-2">
      <InputText v-model="form.username" label="Username" name="username" autocomplete="username" :rules="[required()]" />
      <InputText v-model="form.email" type="email" label="E-Mail" name="email" autocomplete="email" :rules="[required(), email()]" />
      <div v-if="errorMessage" class="c-red">{{ errorMessage }}</div>
      <div class="w-max">
        <InputGroup v-model="form.tos" :rules="[sameAs('You need to accept the Terms and Conditions')(true)]" :silent-errors="false" full-width>
          <InputCheckbox v-model="form.tos">
            <template #label>I agree to the&nbsp;<Link to="/terms">Terms and Conditions</Link></template>
          </InputCheckbox>
        </InputGroup>
      </div>
      <div>
        <Button type="submit" :disabled="loading" @click.prevent="submit">Sign up using {{ data.provider }} account {{ data.username }}</Button>
      </div>
      <Link to="login">Login with existing account</Link>
    </form>

    <div v-if="done" class="flex flex-col gap-2">
      <p>Your account has been created! <template v-if="emailVerificationNeeded">Please check your emails to complete the signup process.</template></p>
      <div class="flex gap-2">
        <Button v-if="data.returnUrl" :to="data.returnUrl">Back to last page</Button>
        <Button to="/auth/settings/account">Go to account settings</Button>
      </div>
    </div>
  </Card>
</template>
