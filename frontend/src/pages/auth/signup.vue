<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useRoute, useRouter } from "vue-router";
import { reactive, ref } from "vue";
import { useSeo } from "~/composables/useSeo";
import Card from "~/lib/components/design/Card.vue";
import InputText from "~/lib/components/ui/InputText.vue";
import Button from "~/lib/components/design/Button.vue";
import { useInternalApi } from "~/composables/useApi";

const route = useRoute();
const router = useRouter();

interface SignupForm {
  username?: string;
  email?: string;
  password?: string;
}

const form = reactive<SignupForm>({});
const repeatedPassword = ref<string>();
const loading = ref(false);

async function submit() {
  loading.value = true;
  try {
    // TODO validation
    await useInternalApi("auth/signup", "POST", form);
    // TODO error handling
    await router.push("/auth/settings");
  } catch (e) {
    console.log("error", e);
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

    <form>
      <InputText v-model="form.username" label="Username" name="username" autocomplete="username" />
      <InputText v-model="form.email" type="email" label="E-Mail" name="email" autocomplete="email" />
      <!-- todo port password input -->
      <InputText v-model="form.password" type="password" label="Password" name="new-password" />
      <!-- todo check password matching -->
      <InputText v-model="repeatedPassword" type="password" label="Password (repeated)" name="repeated-password" />

      <Button type="submit" :disabled="loading" @click.prevent="submit">Sign up</Button>
      <Button type="button" button-type="secondary" @click="login">Login</Button>
    </form>
  </Card>
</template>
