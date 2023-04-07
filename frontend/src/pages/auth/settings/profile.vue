<script lang="ts" setup>
import { reactive, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useVuelidate } from "@vuelidate/core";
import { useInternalApi } from "~/composables/useApi";
import { useAuthStore } from "~/store/auth";
import { useNotificationStore } from "~/store/notification";
import { required } from "~/composables/useValidationHelpers";
import AvatarChangeModal from "~/components/modals/AvatarChangeModal.vue";
import InputText from "~/components/ui/InputText.vue";
import InputSelect from "~/components/ui/InputSelect.vue";
import Button from "~/components/design/Button.vue";
import { definePageMeta } from "#imports";

definePageMeta({
  globalPermsRequired: ["EDIT_OWN_USER_SETTINGS"],
});

const auth = useAuthStore();
const notification = useNotificationStore();
const { t } = useI18n();
const v = useVuelidate();

const loading = ref(false);

const profileForm = reactive({
  tagline: auth.user?.tagline,
  socials: Object.entries(auth.user?.socials),
});
const linkType = ref<string>();
const linkTypes = [
  { value: "discord", text: "Discord" },
  { value: "github", text: "GitHub" },
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
    notification.error(e);
  }
  loading.value = false;
}
</script>

<template>
  <div v-if="auth.user">
    <h2 class="text-xl font-bold mb-4">{{ t("auth.settings.profile.header") }}</h2>
    <form class="flex flex-col gap-2">
      <div class="relative mr-3">
        <UserAvatar :username="auth.user.name" :avatar-url="auth.user.avatarUrl" />
        <AvatarChangeModal :avatar="auth.user.avatarUrl" :action="`users/${auth.user.name}/settings/avatar`">
          <template #activator="{ on }">
            <Button class="absolute bottom-0" @click.prevent="on.click"><IconMdiPencil /></Button>
          </template>
        </AvatarChangeModal>
      </div>

      <InputText v-model="profileForm.tagline" label="Tagline" />

      <h3 class="text-lg font-bold mt-2">Social</h3>
      <div v-for="(link, idx) in profileForm.socials" :key="link[0]" class="flex gap-2 items-center">
        <span class="basis-1/3">{{ linkTypes.find((e) => e.value === link[0])?.text }}</span>
        <InputText v-model="link[1]" label="Username" :rules="[required()]" />
        <IconMdiBin class="w-8 h-8 cursor-pointer" @click="removeLink(idx)" />
      </div>
      <div class="flex gap-2 items-center">
        <div class="basis-1/3">
          <Button button-type="secondary" @click.prevent="addLink">Add link</Button>
        </div>
        <InputSelect v-model="linkType" :values="linkTypes" label="Type" />
        <span class="w-8 h-8" />
      </div>

      <Button type="submit" class="w-max mt-2" :disabled="loading" @click.prevent="saveProfile">Save</Button>
    </form>
  </div>
</template>
