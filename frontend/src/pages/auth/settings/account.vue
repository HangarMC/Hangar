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
import { definePageMeta } from "#imports";

definePageMeta({
  globalPermsRequired: ["EDIT_OWN_USER_SETTINGS"],
});
const emit = defineEmits<{
  openEmailConfirmModal: () => void;
}>();

const auth = useAuthStore();
const notification = useNotificationStore();
const { t } = useI18n();
const v = useVuelidate();

const loading = ref(false);

const settings = await useAuthSettings();
const accountForm = reactive({
  username: auth.user?.name,
  email: auth.user?.email,
  currentPassword: "",
  newPassword: "",
});

function saveAccount() {
  loading.value = true;
  try {
    // todo saveAccount
  } catch (e) {
    notification.error(e);
  }
  loading.value = false;
}
</script>

<template>
  <div v-if="auth.user">
    <h2 class="text-xl font-bold mb-4">{{ t("auth.settings.account.header") }}</h2>
    <form class="flex flex-col gap-2">
      <InputText v-model="accountForm.username" label="Username" />
      <InputText v-model="accountForm.email" label="Email" autofill="username" autocomplete="username" />
      <Button v-if="!settings?.emailConfirmed" class="w-max" size="small" :disabled="loading" @click.prevent="emit.openEmailConfirmModal">
        Confirm email
      </Button>
      <InputPassword
        v-model="accountForm.currentPassword"
        label="Current password"
        name="current-password"
        autofill="current-password"
        autocomplete="current-password"
      />
      <InputPassword
        v-model="accountForm.newPassword"
        label="New password (optional)"
        name="new-password"
        autofill="new-password"
        autocomplete="new-passwsord"
      />
      <Button type="submit" class="w-max" :disabled="loading" @click.prevent="saveAccount">Save</Button>
    </form>
  </div>
</template>
