<script lang="ts" setup>
import { cloneDeep, isEqual } from "lodash-es";
import type { SettingsResponse } from "#shared/types/backend";
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
const pristine = ref(cloneDeep(toRaw(profileForm)));

// SocialForm drops retired link types on mount, so the baseline is only settled once children have set up
onMounted(() => (pristine.value = cloneDeep(toRaw(profileForm))));

const isDirty = computed(() => profileForm.tagline !== pristine.value.tagline || !isEqual(profileForm.socials, pristine.value.socials));

// SocialForm keeps its own draft of the fields, so it has to be remounted to pick the reverted values back up
const socialFormKey = ref(0);

function discard() {
  profileForm.tagline = pristine.value.tagline;
  profileForm.socials = cloneDeep(pristine.value.socials);
  socialFormKey.value++;
  v.value.$reset();
}

async function saveProfile() {
  if (!(await v.value.$validate())) return;
  loading.value = true;
  try {
    await useInternalApi("users/" + auth.user?.name + "/settings/profile", "POST", profileForm);
    pristine.value = cloneDeep(toRaw(profileForm));
    notification.success(t("general.saved"));
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
    <EditableAvatar :username="auth.user.name" :avatar-url="auth.user.avatarUrl" :action="`users/${auth.user.name}/settings/avatar`" size="xl" />

    <h3 class="text-lg font-bold mt-4 mb-2">{{ t("auth.settings.profile.tagline") }}</h3>
    <InputText v-model="profileForm.tagline" :label="t('auth.settings.profile.tagline')" counter :maxlength="useBackendData.validations.userTagline.max" />

    <SocialForm :key="socialFormKey" v-model="profileForm.socials!" />

    <UnsavedChanges :show="isDirty" :loading="loading" :disabled="v.$error" @save="saveProfile" @discard="discard" />
  </div>
</template>
