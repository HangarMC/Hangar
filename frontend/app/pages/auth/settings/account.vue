<script lang="ts" setup>
import { isAxiosError } from "axios";
import type { SettingsResponse } from "#shared/types/backend";

const emit = defineEmits<{
  openEmailConfirmModal: [];
  refreshSettings: [];
}>();

const props = defineProps<{
  settings?: SettingsResponse;
}>();

const auth = useAuthStore();
const notification = useNotificationStore();
const i18n = useI18n();
const { t } = useI18n();
const v = useVuelidate();
const route = useRoute("auth-settings");
const router = useRouter();

const loading = ref(false);
const error = ref<string>();
const deletionLoading = ref(false);
const deletionConfirmation = ref("");
const deletionModal = useTemplateRef("deletionModal");

const deletionDate = computed(() => {
  if (!props.settings?.deletionScheduledFor) return;
  return new Intl.DateTimeFormat(i18n.locale.value, { dateStyle: "long", timeStyle: "short" }).format(new Date(props.settings.deletionScheduledFor));
});

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

async function requestDeletion() {
  deletionLoading.value = true;
  try {
    await useInternalApi("auth/account/delete", "POST", { content: deletionConfirmation.value });
    deletionModal.value!.isOpen = false;
    deletionConfirmation.value = "";
    emit("refreshSettings");
    notification.success(t("auth.settings.account.deletion.requested"));
  } catch (err) {
    if (isAxiosError(err) && err.response?.data?.message === "error.privileged") {
      await router.push(useAuth.loginUrl(route.path) + "&privileged=true");
    } else {
      notification.fromError(i18n, err);
    }
  }
  deletionLoading.value = false;
}

async function cancelDeletion() {
  deletionLoading.value = true;
  try {
    await useInternalApi("auth/account/delete/cancel", "POST");
    emit("refreshSettings");
    notification.success(t("auth.settings.account.deletion.cancelled"));
  } catch (err) {
    notification.fromError(i18n, err);
  }
  deletionLoading.value = false;
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

    <section class="mt-8 rounded-lg border-2 border-red-600 p-5">
      <h2 class="text-xl font-bold text-red-600 dark:text-red-400">{{ t("auth.settings.account.deletion.title") }}</h2>

      <template v-if="settings?.deletionScheduledFor">
        <p class="mt-2 text-lg">{{ t("auth.settings.account.deletion.scheduled", { date: deletionDate }) }}</p>
        <Button button-type="secondary" class="mt-4" :loading="deletionLoading" @click="cancelDeletion">
          {{ t("auth.settings.account.deletion.cancel") }}
        </Button>
      </template>

      <template v-else>
        <p class="mt-2">
          {{ t("auth.settings.account.deletion.description", { count: settings?.ownedProjectCount ?? 0 }) }}
        </p>
        <p v-if="settings?.ownedOrganizationCount" class="mt-3 font-semibold text-red-600 dark:text-red-400">
          {{ t("auth.settings.account.deletion.organizationWarning", { count: settings.ownedOrganizationCount }) }}
        </p>
        <Button
          button-type="red"
          class="mt-4"
          :disabled="!settings || !!settings.ownedOrganizationCount"
          :loading="deletionLoading"
          @click="deletionModal!.isOpen = true"
        >
          {{ t("auth.settings.account.deletion.button") }}
        </Button>
      </template>
    </section>

    <Modal ref="deletionModal" :title="t('auth.settings.account.deletion.modalTitle')">
      <p class="mb-3">{{ t("auth.settings.account.deletion.modalWarning") }}</p>
      <InputText
        v-model="deletionConfirmation"
        :label="t('auth.settings.account.deletion.confirmation', { username: auth.user.name })"
        autocomplete="off"
      />
      <Button
        button-type="red"
        class="mt-3"
        :disabled="deletionConfirmation !== auth.user.name"
        :loading="deletionLoading"
        @click="requestDeletion"
      >
        {{ t("general.confirm") }}
      </Button>
    </Modal>
  </div>
</template>
