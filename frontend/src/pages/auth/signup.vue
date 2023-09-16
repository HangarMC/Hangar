<script lang="ts" setup>
import { useHead } from "@unhead/vue";
import { useRoute, useRouter } from "vue-router";
import { reactive, ref } from "vue";
import { useVuelidate } from "@vuelidate/core";
import { useI18n } from "vue-i18n";
import { useSeo } from "~/composables/useSeo";
import Card from "~/components/design/Card.vue";
import InputText from "~/components/ui/InputText.vue";
import Button from "~/components/design/Button.vue";
import { useInternalApi } from "~/composables/useApi";
import InputPassword from "~/components/ui/InputPassword.vue";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import { email, required, sameAs } from "~/composables/useValidationHelpers";
import { useNotificationStore } from "~/store/notification";
import Link from "~/components/design/Link.vue";
import InputGroup from "~/components/ui/InputGroup.vue";

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

const notification = useNotificationStore();
const i18n = useI18n();
const form = reactive<SignupForm>({});
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
    notification.fromError(i18n, e);
  }
  loading.value = false;
}

useHead(useSeo("Signup", null, route, null));
</script>

<template>
  <Card class="w-xl mx-auto max-w-full">
    <template #header>
      <h1>Sign up</h1>
    </template>

    <form v-if="!done" class="flex flex-col gap-2">
      <InputText v-model="form.username" label="Username" name="username" autocomplete="username" :rules="[required()]" />
      <InputText v-model="form.email" type="email" label="E-Mail" name="email" autocomplete="email" :rules="[required(), email()]" />
      <InputPassword v-model="form.password" label="Password" name="new-password" :rules="[required()]" />
      <div v-if="errorMessage" class="c-red">{{ errorMessage }}</div>
      <div class="w-max">
        <InputGroup v-model="form.tos" :rules="[sameAs('You need to accept the Terms and Conditions')(true)]" :silent-errors="false" full-width>
          <InputCheckbox v-model="form.tos">
            <template #label>I agree to the&nbsp;<Link to="/terms">Terms and Conditions</Link></template>
          </InputCheckbox>
        </InputGroup>
      </div>
      <div>
        <Button type="submit" :disabled="loading" @click.prevent="submit">Sign up</Button>
      </div>
      <Link to="login">Login with existing account</Link>
    </form>

    <div v-if="done" class="flex flex-col gap-2">
      <p>Your account has been created! Please check your emails to complete the signup process.</p>
      <div class="flex gap-2">
        <Button v-if="route.query.returnUrl" :to="route.query.returnUrl">Back to last page</Button>
        <Button to="/auth/settings/account">Go to account settings</Button>
      </div>
    </div>
  </Card>
</template>
