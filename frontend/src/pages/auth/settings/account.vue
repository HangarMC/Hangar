<script lang="ts" setup>
import { reactive, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useVuelidate } from "@vuelidate/core";
import { useAuthStore } from "~/store/auth";
import { useNotificationStore } from "~/store/notification";
import { useAuthSettings } from "~/composables/useApiHelper";
import Button from "~/components/design/Button.vue";
import InputPassword from "~/components/ui/InputPassword.vue";
import InputText from "~/components/ui/InputText.vue";
import { email, required, useInternalApi } from "#imports";
import PageTitle from "~/components/design/PageTitle.vue";

const emit = defineEmits<{
  openEmailConfirmModal: () => void;
}>();

const auth = useAuthStore();
const notification = useNotificationStore();
const i18n = useI18n();
const { t } = useI18n();
const v = useVuelidate();

const loading = ref(false);
const error = ref<string>();

const settings = await useAuthSettings();
const accountForm = reactive({
  username: auth.user?.name,
  email: auth.user?.email,
  currentPassword: "",
  newPassword: "",
});

async function saveAccount() {
  if (!(await v.value.$validate())) return;
  loading.value = true;
  error.value = undefined;
  try {
    await useInternalApi("auth/account", "POST", accountForm);
    notification.success("Saved!");
    accountForm.currentPassword = "";
    accountForm.newPassword = "";
    v.value.$reset();
  } catch (e) {
    notification.fromError(i18n, e);
  }
  loading.value = false;
}
</script>

<template>
  <div v-if="auth.user">
    <PageTitle>{{ t("auth.settings.account.header") }}</PageTitle>
    <form class="flex flex-col gap-2">
      <InputText v-model="accountForm.username" label="Username" :rules="[required()]" />
      <span class="text-sm opacity-85 -mt-1.5">Note that you can only change your username once every 30 days.</span>
      <InputText v-model="accountForm.email" label="Email" autofill="username" autocomplete="username" :rules="[required(), email()]" />
      <Button v-if="!settings?.emailConfirmed" class="w-max" size="small" :disabled="loading" @click.prevent="$emit('openEmailConfirmModal')">
        Verify email
      </Button>
      <InputPassword
        v-model="accountForm.currentPassword"
        label="Current password"
        name="current-password"
        autofill="current-password"
        autocomplete="current-password"
        :rules="[required()]"
      />
      <InputPassword
        v-model="accountForm.newPassword"
        label="New password (optional)"
        name="new-password"
        autofill="new-password"
        autocomplete="new-password"
      />
      <div v-if="error" class="text-red">{{ error }}</div>
      <Button type="submit" class="w-max" :disabled="loading" @click.prevent="saveAccount">Save</Button>
    </form>
  </div>
</template>
