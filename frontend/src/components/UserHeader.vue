<script lang="ts" setup>
import { NamedPermission } from "~/types/backend";
import type { HangarOrganization, User } from "~/types/backend";

const props = defineProps<{
  viewingUser?: User;
  organization?: HangarOrganization;
}>();

const i18n = useI18n();
const authStore = useAuthStore();

const isCurrentUser = computed<boolean>(() => {
  return authStore.user !== null && authStore.user?.name === props.viewingUser?.name;
});

const canEditCurrentUser = computed<boolean>(() => {
  return hasPerms(NamedPermission.EditAllUserSettings) || isCurrentUser.value || hasPerms(NamedPermission.EditSubjectSettings);
});
</script>

<template>
  <Card accent class="overflow-y-hidden">
    <div class="flex mb-4 md:mb-0">
      <div class="relative mr-3">
        <UserAvatar :username="viewingUser?.name" :avatar-url="viewingUser?.avatarUrl" :loading="!viewingUser" />
        <AvatarChangeModal
          v-if="viewingUser && hasPerms(NamedPermission.EditSubjectSettings)"
          :avatar="viewingUser.avatarUrl"
          :action="`${viewingUser.isOrganization ? 'organizations/org' : 'users'}/${viewingUser.name}/settings/avatar`"
        >
          <template #activator="{ on }">
            <Button class="absolute -bottom-3 -right-3" v-on="on"><IconMdiPencil /></Button>
          </template>
        </AvatarChangeModal>
      </div>

      <div class="overflow-clip overflow-hidden">
        <h1 v-if="viewingUser" class="text-2xl px-1 text-strong inline-flex items-center">
          {{ viewingUser.name }}
          <a
            v-if="viewingUser.socials?.github"
            :href="`https://github.com/${viewingUser.socials.github}`"
            class="ml-1"
            rel="external nofollow"
            title="GitHub Link"
          >
            <IconMdiGithub class="mr-1 hover:text-slate-400" />
          </a>
          <a
            v-if="viewingUser.socials?.twitter"
            :href="`https://twitter.com/${viewingUser.socials.twitter}`"
            class="ml-1"
            rel="external nofollow"
            title="Twitter Link"
          >
            <IconMdiTwitter class="mr-1 hover:text-slate-400" />
          </a>
          <a
            v-if="viewingUser.socials?.youtube"
            :href="`https://youtube.com/${viewingUser.socials.youtube}`"
            class="ml-1"
            rel="external nofollow"
            title="YouTube Link"
          >
            <IconMdiYoutube class="mr-1 hover:text-slate-400" />
          </a>
          <a v-if="viewingUser.socials?.website" :href="viewingUser.socials.website" class="ml-1" rel="external nofollow" title="Website Link">
            <IconMdiWeb class="mr-1 hover:text-slate-400" />
          </a>
          <Tooltip v-if="viewingUser.socials?.discord">
            <template #content>
              <span class="text-base">{{ viewingUser.socials.discord }}</span>
            </template>
            <IconMdiDiscord class="ml-1 hover:text-slate-400" />
          </Tooltip>
          <span v-if="viewingUser.locked" class="ml-1">
            <IconMdiLockOutline />
          </span>
          <Popper v-if="viewingUser.nameHistory?.length > 0" placement="bottom">
            <IconMdiChevronDown cursor="pointer" />
            <template #content>
              <div class="-mt-2.5 p-2 flex flex-col rounded background-default filter shadow-default text-base">
                <div class="font-bold">Was known as:</div>
                <div v-for="(history, idx) of viewingUser.nameHistory" :key="idx">{{ history.oldName }} until <PrettyTime :time="history.date" long /></div>
              </div>
            </template>
          </Popper>
          <SocialsModal
            v-if="canEditCurrentUser"
            :socials="viewingUser.socials"
            :action="`${viewingUser.isOrganization ? 'organizations/org' : 'users'}/${viewingUser.name}/settings/socials`"
          />
        </h1>
        <Skeleton v-else class="text-2xl px-1 w-50" />

        <div v-if="viewingUser" class="ml-1">
          <span v-if="viewingUser.tagline">{{ viewingUser.tagline }}</span>
          <span v-else-if="canEditCurrentUser">{{ i18n.t("author.addTagline") }}</span>
          <TaglineModal
            v-if="canEditCurrentUser"
            :tagline="viewingUser.tagline"
            :action="`${viewingUser.isOrganization ? 'organizations/org' : 'users'}/${viewingUser.name}/settings/tagline`"
          />
        </div>
        <Skeleton v-else class="mt-1 w-100" />
      </div>
      <div class="flex-grow" />
      <div class="lt-md:hidden flex flex-col space-y-1 items-end flex-shrink-0">
        <template v-if="viewingUser">
          <span>{{ i18n.t("author.memberSince", [i18n.d(viewingUser.createdAt, "date")]) }}</span>
          <span>{{ i18n.t("author.numProjects", [viewingUser.projectCount], viewingUser.projectCount) }}</span>
          <span class="inline-flex space-x-1">
            <Tag v-for="roleId in viewingUser.roles" :key="roleId" :color="{ background: getRole(roleId)?.color }" :name="getRole(roleId)?.title" />
          </span>
        </template>
        <template v-else>
          <Skeleton />
          <Skeleton />
        </template>
      </div>
    </div>
    <div v-if="viewingUser" class="md:hidden flex flex-col items-center space-y-1 flex-shrink-0">
      <span>{{ i18n.t("author.memberSince", [i18n.d(viewingUser.createdAt, "date")]) }}</span>
      <span>{{ i18n.t("author.numProjects", [viewingUser.projectCount], viewingUser.projectCount) }}</span>
      <Tag v-for="roleId in viewingUser.roles" :key="roleId" :color="{ background: getRole(roleId)?.color }" :name="getRole(roleId)?.title" />
    </div>
  </Card>
  <hr class="border-gray-400 dark:border-gray-500 my-4 mb-4" />
</template>
