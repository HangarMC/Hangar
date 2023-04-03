<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useRoute, useRouter } from "vue-router";
import { reactive, ref } from "vue";
import { useSeo } from "~/composables/useSeo";
import Card from "~/lib/components/design/Card.vue";
import InputText from "~/lib/components/ui/InputText.vue";
import Button from "~/lib/components/design/Button.vue";
import { useInternalApi } from "~/composables/useApi";
import InputPassword from "~/lib/components/ui/InputPassword.vue";
import InputCheckbox from "~/lib/components/ui/InputCheckbox.vue";
import { email, minLength, required, sameAs } from "~/lib/composables/useValidationHelpers";
import { useVuelidate } from "@vuelidate/core";
import { useNotificationStore } from "~/lib/store/notification";
import InputGroup from "~/lib/components/ui/InputGroup.vue";
import Alert from "~/lib/components/design/Alert.vue";

const route = useRoute();
const router = useRouter();
const v = useVuelidate();

interface SignupForm {
  username?: string;
  email?: string;
  password?: string;
  tos?: boolean;
}

const done = ref(false);

const form = reactive<SignupForm>({});
const repeatedPassword = ref<string>();
const loading = ref(false);

const errorMessage = ref<string | undefined>();

async function submit() {
  if (!(await v.value.$validate())) return;
  loading.value = true;
  errorMessage.value = undefined;
  try {
    await useInternalApi("auth/signup", "POST", form);
    done.value = true;
  } catch (e) {
    if (e.response?.data?.message) {
      errorMessage.value = e.response.data.message;
    } else {
      useNotificationStore().error(e);
    }
  }
  loading.value = false;
}

async function login() {
  await router.push("/auth/login");
}

useHead(useSeo("Signup", null, route, null));
</script>

<template>
  <Card>
    <template #header>
      <h1>Sign up</h1>
    </template>

    <form class="flex flex-col gap-2" v-if="!done">
      <InputText v-model="form.username" label="Username" name="username" autocomplete="username" :rules="[required()]" />
      <InputText v-model="form.email" type="email" label="E-Mail" name="email" autocomplete="email" :rules="[required(), email()]" />
      <InputPassword v-model="form.password" label="Password" name="new-password" :rules="[required()]" />
      <div v-if="errorMessage" class="c-red">{{ errorMessage }}</div>
      <div class="w-max">
        <InputGroup v-model="form.tos" :rules="[sameAs('Please agree to the tos!')(true)]" :silent-errors="false" full-width>
          <InputCheckbox v-model="form.tos" label="Agree to TOS?" />
        </InputGroup>
      </div>
      <div class="flex gap-2">
        <Button type="submit" :disabled="loading" @click.prevent="submit">Sign up</Button>
        <Button type="button" button-type="secondary" @click="login">Login</Button>
      </div>
    </form>

    <div v-if="done" class="flex flex-col gap-2">
      <Alert type="success" class="w-max">Account created!</Alert>
      <p>We have send you an email confirmation</p>
      <div class="flex gap-2">
        <Button v-if="route.query.returnUrl">Back to last page</Button>
        <Button>Goto account settings</Button>
      </div>
    </div>
  </Card>
</template>
