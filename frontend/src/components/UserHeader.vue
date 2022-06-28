<script lang="ts" setup>
import { Organization } from "hangar-internal";
import { User } from "hangar-api";
import UserAvatar from "~/components/UserAvatar.vue";
import { avatarUrl, forumUserUrl } from "~/composables/useUrlHelper";
import { useI18n } from "vue-i18n";
import Card from "~/lib/components/design/Card.vue";
import TaglineModal from "~/components/modals/TaglineModal.vue";
import { computed } from "vue";
import { NamedPermission } from "~/types/enums";
import { hasPerms } from "~/composables/usePerm";
import { useAuthStore } from "~/store/auth";
import Tag from "~/components/Tag.vue";
import AvatarChangeModal from "~/components/modals/AvatarChangeModal.vue";

const props = defineProps<{
  user: User;
  organization: Organization;
}>();

const i18n = useI18n();
const authStore = useAuthStore();

const isCurrentUser = computed<boolean>(() => {
  return authStore.user !== null && authStore.user.name === props.user.name;
});

const canEditCurrentUser = computed<boolean>(() => {
  return hasPerms(NamedPermission.EDIT_ALL_USER_SETTINGS) || isCurrentUser.value || hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS);
});
</script>

<template>
  <Card accent class="overflow-y-hidden">
    <div class="flex mb-4 md:mb-0">
      <div class="relative">
        <UserAvatar :username="user.name" :avatar-url="avatarUrl(user.name)" />
        <AvatarChangeModal v-if="user.isOrganization && hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS)" :user="user" />
      </div>

      <div class="ml-2 overflow-clip overflow-hidden">
        <h1 class="text-2xl text-strong inline-flex items-center">
          {{ user.name }}
          <a v-if="!user.isOrganization" class="inline-flex mx-1" :href="forumUserUrl(user.name)" :title="i18n.t('author.viewOnForums')">
            <IconMdiOpenInNew />
          </a>
          <span v-if="user.locked">
            <IconMdiLockOutline />
          </span>
        </h1>

        <div>
          <span v-if="user.tagline">{{ user.tagline }}</span>
          <span v-else-if="canEditCurrentUser">{{ i18n.t("author.addTagline") }}</span>
          <TaglineModal
            v-if="canEditCurrentUser"
            :tagline="user.tagline"
            :action="`${user.isOrganization ? 'organizations/org' : 'users'}/${user.name}/settings/tagline`"
          />
        </div>
      </div>
      <div class="flex-grow" />
      <div class="<md:hidden flex flex-col space-y-1 items-end flex-shrink-0">
        <span>{{ i18n.t("author.memberSince", [i18n.d(user.joinDate, "date")]) }}</span>
        <span>{{ i18n.t("author.numProjects", [user.projectCount], user.projectCount) }}</span>
        <Tag v-for="role in user.roles" :key="role.roleId" :color="{ background: role.color }" :name="role.title" />
      </div>
    </div>
    <div class="md:hidden flex flex-col items-center space-y-1 flex-shrink-0">
      <span>{{ i18n.t("author.memberSince", [i18n.d(user.joinDate, "date")]) }}</span>
      <span>{{ i18n.t("author.numProjects", [user.projectCount], user.projectCount) }}</span>
      <Tag v-for="role in user.roles" :key="role.roleId" :color="{ background: role.color }" :name="role.title" />
    </div>
  </Card>
  <hr class="border-gray-400 dark:border-gray-500 my-4 mb-4" />
</template>
