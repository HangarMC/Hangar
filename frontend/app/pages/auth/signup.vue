<script lang="ts" setup>
const route = useRoute("auth-signup");
const v = useVuelidate();
const backendData = useBackendData;

interface SignupForm {
  username?: string;
  email?: string;
  password?: string;
  tos?: boolean;
  captcha?: string;
}

const config = useRuntimeConfig();

const done = ref(false);

const notification = useNotificationStore();
const i18n = useI18n();
const form = reactive<SignupForm>({});
const loading = ref(false);
const turnstile = useTemplateRef("turnstile");

const errorMessage = ref<string | undefined>();

async function submit() {
  if (!(await v.value.$validate())) return;
  loading.value = true;
  errorMessage.value = undefined;
  try {
    await useInternalApi("auth/signup", "POST", form);
    done.value = true;
  } catch (err: any) {
    notification.fromError(i18n, err);
    if (err?.response?.data?.message === "error.captcha") {
      turnstile.value?.reset();
    }
  }
  loading.value = false;
}

useSeo(computed(() => ({ title: "Sign up", route })));
</script>

<template>
  <Card class="w-xl mx-auto max-w-full">
    <template #header>
      <h1>Sign up</h1>
    </template>

    <div v-if="done" class="flex flex-col gap-2">
      <p>Your account has been created! Please check your emails to complete the signup process.</p>
      <div class="flex gap-2">
        <Button v-if="route.query.returnUrl" :to="route.query.returnUrl">Back to last page</Button>
        <Button to="/auth/settings/account">Go to account settings</Button>
      </div>
    </div>

    <div v-else class="flex flex-col gap-2">
      Login using an OAuth provider
      <div class="flex flex-row gap-x-4">
        <Button
          v-for="provider in backendData.security.oauthProviders"
          :key="provider"
          :disabled="loading"
          :href="'/api/internal/oauth/' + provider + '/login?mode=login&returnUrl=' + (route.query.returnUrl ? route.query.returnUrl : '/')"
        >
          <template v-if="provider === 'github'">
            <div class="flex flex-row gap-x-0.5 items-center">
              <IconMdiGithub class="mr-1" />
              GitHub
            </div>
          </template>
          <template v-if="provider === 'google'">
            <div class="flex flex-row gap-x-0.5 items-center">
              <IconMdiGoogle class="mr-1" />
              Google
            </div>
          </template>
          <template v-if="provider === 'microsoft'">
            <div class="flex flex-row gap-x-0.5 items-center">
              <IconMdiMicrosoft class="mr-1" />
              Microsoft
            </div>
          </template>
        </Button>
      </div>

      <div class="flex items-center space-x-2">
        <hr class="flex-grow border-zinc-200 dark:border-zinc-700" />
        <span class="text-zinc-400 dark:text-zinc-300 text-sm">OR</span>
        <hr class="flex-grow border-zinc-200 dark:border-zinc-700" />
      </div>
      Create a new account

      <form class="flex flex-col gap-2">
        <InputText v-model="form.username" label="Username" name="username" autocomplete="username" :rules="[required()]" />
        <InputText v-model="form.email" type="email" label="E-Mail" name="email" autocomplete="email" :rules="[required(), email()]" />
        <InputPassword v-model="form.password" label="Password" name="new-password" :rules="[required()]" />
        <LazyNuxtTurnstile v-if="config.public.turnstile?.siteKey != '1x00000000000000000000AA'" ref="turnstile" v-model="form.captcha" />
        <div v-if="errorMessage" class="c-red">{{ errorMessage }}</div>
        <Button type="submit" :disabled="loading" @click.prevent="submit">Sign up</Button>
        <div class="w-max">
          <InputGroup v-model="form.tos" :rules="[sameAs('You need to accept the Terms and Conditions')(true)]" :silent-errors="false" full-width>
            <InputCheckbox v-model="form.tos">
              <template #label>I agree to the&nbsp;<Link to="/terms">Terms and Conditions</Link></template>
            </InputCheckbox>
          </InputGroup>
        </div>

        <hr class="flex-grow border-zinc-200 dark:border-zinc-700 mt-1" />

        <Link to="login">Login with existing account</Link>
      </form>
    </div>
  </Card>
</template>
