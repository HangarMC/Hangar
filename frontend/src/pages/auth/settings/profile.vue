<script lang="ts" setup>
import type { SettingsResponse } from "~/types/backend";
import SocialForm from "~/components/form/SocialForm.vue";

defineProps<{
  settings?: SettingsResponse;
}>();

const auth = useAuthStore();
const notification = useNotificationStore();
const i18n = useI18n();
const { t } = useI18n();
const v = useVuelidate();

const loading = ref(false);

const profileForm = reactive({
  tagline: auth.user?.tagline,
  socials: auth.user?.socials,
});

async function saveProfile() {
  if (!(await v.value.$validate())) return;
  loading.value = true;
  try {
    await useInternalApi("users/" + auth.user?.name + "/settings/profile", "POST", profileForm);
    notification.success("Saved!");
  } catch (err) {
    notification.fromError(i18n, err);
  }
  loading.value = false;
}
</script>

<template>
  <div v-if="auth.user">
    <PageTitle>{{ t("auth.settings.profile.header") }}</PageTitle>

    <h3 class="text-lg font-bold mb-2">{{ t("auth.settings.profile.avatar") }}</h3>
    <div class="relative">
      <UserAvatar :username="auth.user.name" :avatar-url="auth.user.avatarUrl" />
      <AvatarChangeModal :avatar="auth.user.avatarUrl" :action="`users/${auth.user.name}/settings/avatar`">
        <template #activator="{ on }">
          <Button class="absolute bottom-0" @click.prevent="on.click"><IconMdiPencil /></Button>
        </template>
      </AvatarChangeModal>
    </div>

    <h3 class="text-lg font-bold mt-4 mb-2">{{ t("auth.settings.profile.tagline") }}</h3>
    <InputText v-model="profileForm.tagline" :label="t('auth.settings.profile.tagline')" counter :maxlength="useBackendData.validations.userTagline.max" />

    <h3 class="text-lg font-bold mt-4">{{ t("auth.settings.profile.social") }}</h3>
    <SocialForm v-model="profileForm.socials" />

    <Button type="submit" class="w-max mt-2" :disabled="loading" @click.prevent="saveProfile">{{ t("general.save") }}</Button>
  </div>
</template>
