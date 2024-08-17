<script lang="ts" setup>
import type { SettingsResponse } from "~/types/backend";

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
  socials: auth.user?.socials ? Object.entries(auth.user.socials) : [],
});
const linkType = ref<string>();
const linkTypes = [
  { value: "discord", text: "Discord" },
  { value: "github", text: "GitHub" },
  { value: "twitter", text: "Twitter" },
  { value: "youtube", text: "YouTube" },
  { value: "website", text: "Website" },
];

function addLink() {
  if (!linkType.value) {
    return notification.error("You have to select a type");
  }
  if (profileForm.socials.some((e) => (e[0] as string) === linkType.value)) {
    return notification.error("You already have a link of that type added");
  }
  profileForm.socials.push([linkType.value, ""]);
}

function removeLink(idx: number) {
  profileForm.socials.splice(idx, 1);
}

async function saveProfile() {
  if (!(await v.value.$validate())) return;
  loading.value = true;
  try {
    await useInternalApi("users/" + auth.user?.name + "/settings/profile", "POST", profileForm);
    notification.success("Saved!");
  } catch (e) {
    notification.fromError(i18n, e);
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
    <div v-for="(link, idx) in profileForm.socials" :key="link[0]" class="flex items-center mt-2">
      <span class="w-25">{{ linkTypes.find((e) => e.value === link[0])?.text }}</span>
      <div class="w-75">
        <InputText v-if="link[0] === 'website'" v-model="link[1]" label="URL" :rules="[required(), validUrl()]" />
        <InputText v-else v-model="link[1]" :label="t('auth.settings.account.username')" :rules="[required()]" />
      </div>
      <IconMdiBin class="ml-2 w-6 h-6 cursor-pointer hover:color-red" @click="removeLink(idx)" />
    </div>
    <div class="flex items-center mt-2">
      <div class="w-25">
        <Button button-type="secondary" @click.prevent="addLink">Add link</Button>
      </div>
      <div class="w-75">
        <InputSelect v-model="linkType" :values="linkTypes" :label="t('project.settings.links.typeField')" />
      </div>
    </div>

    <Button type="submit" class="w-max mt-2" :disabled="loading" @click.prevent="saveProfile">{{ t("general.save") }}</Button>
  </div>
</template>
