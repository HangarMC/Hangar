<script lang="ts" setup>
import type { Organization } from "hangar-internal";
import type { User } from "hangar-api";
import { useI18n } from "vue-i18n";
import { computed } from "vue";
import UserAvatar from "~/components/UserAvatar.vue";
import Card from "~/components/design/Card.vue";
import TaglineModal from "~/components/modals/TaglineModal.vue";
import { NamedPermission } from "~/types/enums";
import { hasPerms } from "~/composables/usePerm";
import { useAuthStore } from "~/store/auth";
import Tag from "~/components/Tag.vue";
import AvatarChangeModal from "~/components/modals/AvatarChangeModal.vue";
import Button from "~/components/design/Button.vue";
import Popper from "~/components/design/Popper.vue";
import { getRole } from "~/store/backendData";
import PrettyTime from "~/components/design/PrettyTime.vue";

const props = defineProps<{
  viewingUser: User;
  organization: Organization;
}>();

const i18n = useI18n();
const authStore = useAuthStore();

const isCurrentUser = computed<boolean>(() => {
  return authStore.user !== null && authStore.user.name === props.viewingUser.name;
});

const canEditCurrentUser = computed<boolean>(() => {
  return hasPerms(NamedPermission.EDIT_ALL_USER_SETTINGS) || isCurrentUser.value || hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS);
});
</script>

<template>
  <Card accent class="overflow-y-hidden">
    <div class="flex mb-4 md:mb-0">
      <div class="relative mr-3">
        <UserAvatar :username="viewingUser.name" :avatar-url="viewingUser.avatarUrl" />
        <AvatarChangeModal
          v-if="hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS)"
          :avatar="viewingUser.avatarUrl"
          :action="`${viewingUser.isOrganization ? 'organizations/org' : 'users'}/${viewingUser.name}/settings/avatar`"
        >
          <template #activator="{ on }">
            <Button class="absolute -bottom-3 -right-3" v-on="on"><IconMdiPencil /></Button>
          </template>
        </AvatarChangeModal>
      </div>

      <div class="overflow-clip overflow-hidden">
        <h1 class="text-2xl px-1 text-strong inline-flex items-center">
          {{ viewingUser.name }}
          <span v-if="viewingUser.locked" class="ml-1">
            <IconMdiLockOutline />
          </span>
          <Popper v-if="viewingUser.nameHistory?.length > 0">
            <IconMdiChevronDown cursor="pointer" />
            <template #content="{ close }">
              <div class="-mt-2.5 p-2 flex flex-col rounded background-default filter shadow-default text-base" @click="close()">
                <div class="font-bold">Was known as:</div>
                <div v-for="(history, idx) of viewingUser.nameHistory" :key="idx">{{ history.oldName }} until <PrettyTime :time="history.date" long /></div>
              </div>
            </template>
          </Popper>
        </h1>

        <div class="ml-1">
          <span v-if="viewingUser.tagline">{{ viewingUser.tagline }}</span>
          <span v-else-if="canEditCurrentUser">{{ i18n.t("author.addTagline") }}</span>
          <TaglineModal
            v-if="canEditCurrentUser"
            :tagline="viewingUser.tagline"
            :action="`${viewingUser.isOrganization ? 'organizations/org' : 'users'}/${viewingUser.name}/settings/tagline`"
          />
        </div>
      </div>
      <div class="flex-grow" />
      <div class="lt-md:hidden flex flex-col space-y-1 items-end flex-shrink-0">
        <span>{{ i18n.t("author.memberSince", [i18n.d(viewingUser.createdAt, "date")]) }}</span>
        <span>{{ i18n.t("author.numProjects", [viewingUser.projectCount], viewingUser.projectCount) }}</span>
        <span class="inline-flex space-x-1">
          <Tag v-for="roleId in viewingUser.roles" :key="roleId" :color="{ background: getRole(roleId).color }" :name="getRole(roleId).title" />
        </span>
      </div>
    </div>
    <div class="md:hidden flex flex-col items-center space-y-1 flex-shrink-0">
      <span>{{ i18n.t("author.memberSince", [i18n.d(viewingUser.createdAt, "date")]) }}</span>
      <span>{{ i18n.t("author.numProjects", [viewingUser.projectCount], viewingUser.projectCount) }}</span>
      <Tag v-for="roleId in viewingUser.roles" :key="roleId" :color="{ background: getRole(roleId).color }" :name="getRole(roleId).title" />
    </div>
  </Card>
  <hr class="border-gray-400 dark:border-gray-500 my-4 mb-4" />
</template>
