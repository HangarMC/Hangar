<script lang="ts" setup>
import type { SettingsResponse } from "#shared/types/backend";

const emit = defineEmits<{
  openEmailConfirmModal: [];
  refreshSettings: [];
}>();

defineProps<{
  settings?: SettingsResponse;
}>();

const auth = useAuthStore();
const notification = useNotificationStore();
const i18n = useI18n();
const { t } = useI18n();
const v = useVuelidate();

const loading = ref(false);
const error = ref<string>();

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
    emit("refreshSettings");
    useAuth.updateUser(true);
    v.value.$reset();
  } catch (err) {
    notification.fromError(i18n, err);
  }
  loading.value = false;
}
</script>

<template>
  <div v-if="auth.user">
    <PageTitle>{{ t("auth.settings.account.header") }}</PageTitle>
    <form class="flex flex-col gap-2">
      <InputText v-model="accountForm.username" :label="t('auth.settings.account.username')" :rules="[required()]" />
      <span class="text-sm opacity-85 -mt-1.5">Note that you can only change your username once every 30 days.</span>
      <InputText v-model="accountForm.email" label="Email" autofill="username" autocomplete="username" :rules="[required(), email()]" />
      <Button v-if="!settings?.emailConfirmed" class="w-max" size="small" :disabled="loading" @click.prevent="$emit('openEmailConfirmModal')">
        {{ t("auth.settings.account.verifyEmail") }}
      </Button>
      <template v-if="settings?.hasPassword">
        <InputPassword
          v-model="accountForm.currentPassword"
          :label="t('auth.settings.account.currentPassword')"
          name="current-password"
          autofill="current-password"
          autocomplete="current-password"
          :rules="[required()]"
        />
        <InputPassword
          v-model="accountForm.newPassword"
          :label="t('auth.settings.account.newPassword')"
          name="new-password"
          autofill="new-password"
          autocomplete="new-password"
        />
      </template>
      <div v-if="error" class="text-red">{{ error }}</div>
      <Button type="submit" class="w-max" :disabled="loading" @click.prevent="saveAccount">{{ t("general.save") }}</Button>
    </form>
  </div>
</template>
