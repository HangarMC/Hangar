<script lang="ts" setup>
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
const clearingAvatar = ref(false);

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

async function clearAvatar() {
  clearingAvatar.value = true;
  try {
    const response = await fetch("/api/internal/avatar/default/default.webp", { cache: "no-cache" });
    if (!response.ok) throw new Error("Could not load the default avatar");

    const form = new FormData();
    form.append("avatar", await response.blob(), "default.webp");
    await useInternalApi(`users/${auth.user?.name}/settings/avatar`, "POST", form, { timeout: 10_000 });
    window.location.reload();
  } catch (err) {
    notification.fromError(i18n, err);
    clearingAvatar.value = false;
  }
}
</script>

<template>
  <div v-if="auth.user" class="min-w-0">
    <div class="mb-4">
      <PageTitle>{{ t("auth.settings.profile.header") }}</PageTitle>
      <p class="text-sm text-gray">Manage how your profile appears to other Hangar users.</p>
    </div>

    <div class="grid grid-cols-1 items-start gap-4 xl:grid-cols-2">
      <div class="space-y-4">
        <Card class="!p-0 overflow-hidden">
          <div class="flex flex-col items-start gap-4 p-4 sm:flex-row">
            <UserAvatar class="h-24 w-24 flex-shrink-0 self-start shadow-lg" :username="auth.user.name" :avatar-url="auth.user.avatarUrl" />
            <div class="min-w-0 flex-grow">
              <h2 class="text-xl font-bold">{{ t("auth.settings.profile.avatar") }}</h2>
              <p class="mt-1 text-sm text-gray">PNG, JPG, and WebP images are supported. Your image will be cropped to a square.</p>
              <div class="mt-3 flex items-center gap-2">
                <AvatarChangeModal :avatar="auth.user.avatarUrl" :action="`users/${auth.user.name}/settings/avatar`">
                  <template #activator="{ on }">
                    <Button size="small" @click.prevent="on.click"> Change avatar </Button>
                  </template>
                </AvatarChangeModal>
                <button
                  class="inline-flex h-7.5 w-7.5 flex-shrink-0 items-center justify-center rounded-md border border-transparent transition-all duration-250 hover:scale-[1.015] hover:border-red-600 hover:bg-red-900/50"
                  title="Clear avatar"
                  :disabled="clearingAvatar"
                  @click.prevent="clearAvatar"
                >
                  <IconMdiBin />
                </button>
              </div>
            </div>
          </div>
        </Card>

        <Card>
          <div>
            <h2 class="text-xl font-bold">Username</h2>
            <p class="mt-1 text-sm text-gray">This is how you are identified across Hangar.</p>
            <div class="relative mt-3 flex h-10.5 w-full rounded-md">
              <input
                :value="auth.user.name"
                class="min-w-0 flex-grow rounded-lg border border-transparent bg-gray-100 px-3 py-2 text-gray outline-none dark:bg-gray-800"
                readonly
              />
            </div>
            <div
              class="mt-3 flex items-start gap-2 rounded-lg border border-gray-200 bg-gray-100/60 px-3 py-2 text-sm dark:border-gray-800 dark:bg-charcoal-500/60"
            >
              <IconMdiInformationOutline class="mt-0.5 flex-shrink-0 text-gray" />
              <span>Usernames can only be changed once every 30 days.</span>
            </div>
          </div>

          <div class="mt-5 border-t pt-4 dark:border-gray-800">
            <div class="flex items-start justify-between gap-3">
              <div>
                <h2 class="text-xl font-bold">{{ t("auth.settings.profile.tagline") }}</h2>
                <p class="mt-1 text-sm text-gray">A short description shown below your name on your profile.</p>
              </div>
              <span class="text-xs text-gray"> {{ profileForm.tagline?.length || 0 }}/{{ useBackendData.validations.userTagline.max }} </span>
            </div>
            <div class="relative mt-3 flex h-10.5 rounded-md transition-all duration-200 hover:scale-[1.002]">
              <input
                v-model="profileForm.tagline"
                class="min-w-0 flex-grow truncate rounded-lg border border-transparent bg-gray-100 px-3 py-2 outline-none transition-colors hover:border-gray-300 focus:border-primary dark:bg-gray-800 dark:hover:border-gray-700"
                :maxlength="useBackendData.validations.userTagline.max"
                placeholder="Tell people a little about yourself"
                type="text"
              />
            </div>
          </div>

          <div class="mt-4 flex justify-end border-t pt-4 dark:border-gray-800">
            <Button type="submit" size="medium" :disabled="loading" :loading="loading" @click.prevent="saveProfile">
              <IconMdiContentSaveOutline class="mr-1" />
              {{ t("general.save") }}
            </Button>
          </div>
        </Card>
      </div>

      <Card class="!p-0 overflow-hidden">
        <div class="border-b px-4 py-3 dark:border-gray-800">
          <h2 class="text-xl font-bold">{{ t("auth.settings.profile.social") }}</h2>
          <p class="mt-1 text-sm text-gray">Show your social accounts or website on your public profile.</p>
        </div>
        <div class="p-4">
          <SocialForm v-model="profileForm.socials!">
            <template #actions>
              <Button type="submit" size="medium" :disabled="loading" :loading="loading" @click.prevent="saveProfile">
                <IconMdiContentSaveOutline class="mr-1" />
                {{ t("general.save") }}
              </Button>
            </template>
          </SocialForm>
        </div>
      </Card>
    </div>
  </div>
</template>
