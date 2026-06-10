<script lang="ts" setup>
import { NamedPermission } from "#shared/types/backend";
import type { HangarOrganization, User } from "#shared/types/backend";

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

const editProfileRoute = computed(() => {
  if (!props.viewingUser || props.viewingUser.isOrganization) return undefined;
  if (isCurrentUser.value) return "/auth/settings/profile";
  if (hasPerms(NamedPermission.EditAllUserSettings)) return `/admin/user/${props.viewingUser.name}`;
  return undefined;
});
</script>

<template>
  <Card class="profile-hero mb-4 overflow-visible !p-0">
    <div class="relative z-1 flex flex-col gap-4 p-5 sm:flex-row sm:items-stretch">
      <UserAvatar
        class="h-24 w-24 flex-shrink-0 self-start shadow-lg"
        :username="viewingUser?.name"
        :avatar-url="viewingUser?.avatarUrl"
        :loading="!viewingUser"
      />

      <div class="min-w-0 flex-grow self-start">
        <div v-if="viewingUser" class="flex flex-wrap items-center gap-2">
          <h1 class="truncate text-3xl font-bold">{{ viewingUser.name }}</h1>
          <IconMdiLockOutline v-if="viewingUser.locked" class="text-gray" />
          <Popper v-if="viewingUser.nameHistory?.length > 0" placement="bottom">
            <button class="inline-flex rounded-md p-1 text-gray transition-colors hover:background-card"><IconMdiChevronDown /></button>
            <template #content>
              <div class="background-default flex flex-col gap-1 rounded-lg border p-2 text-sm shadow-lg dark:border-gray-800">
                <div class="font-bold">Was known as:</div>
                <div v-for="(history, idx) of viewingUser.nameHistory" :key="idx">{{ history.oldName }} until <PrettyTime :time="history.date" long /></div>
              </div>
            </template>
          </Popper>
        </div>
        <Skeleton v-else class="h-9 w-50" />

        <div v-if="viewingUser" class="mt-1 flex min-w-0 items-center text-gray-300">
          <span v-if="viewingUser.tagline" class="truncate">{{ viewingUser.tagline }}</span>
          <span v-else-if="canEditCurrentUser">{{ i18n.t("author.addTagline") }}</span>
        </div>
        <Skeleton v-else class="mt-1 w-100" />

        <div v-if="viewingUser" class="mt-3 flex flex-wrap items-center gap-3 text-xl text-gray-300">
          <a
            v-if="viewingUser.socials?.github"
            :href="`https://github.com/${viewingUser.socials.github}`"
            class="transition-colors hover:text-white"
            rel="external nofollow"
            title="GitHub"
          >
            <IconMdiGithub />
          </a>
          <a
            v-if="viewingUser.socials?.twitter"
            :href="`https://twitter.com/${viewingUser.socials.twitter}`"
            class="transition-colors hover:text-white"
            rel="external nofollow"
            title="Twitter"
          >
            <IconMdiTwitter />
          </a>
          <a
            v-if="viewingUser.socials?.youtube"
            :href="`https://youtube.com/${viewingUser.socials.youtube}`"
            class="transition-colors hover:text-white"
            rel="external nofollow"
            title="YouTube"
          >
            <IconMdiYoutube />
          </a>
          <a
            v-if="viewingUser.socials?.website"
            :href="linkout(viewingUser.socials.website)"
            class="transition-colors hover:text-white"
            rel="external nofollow"
            title="Website"
          >
            <IconMdiWeb />
          </a>
          <Tooltip v-if="viewingUser.socials?.discord">
            <template #content
              ><span class="text-base">{{ viewingUser.socials.discord }}</span></template
            >
            <span class="inline-flex"><IconMdiDiscord /></span>
          </Tooltip>
        </div>
      </div>

      <div v-if="viewingUser" class="flex flex-shrink-0 flex-col items-start gap-3 sm:min-h-24 sm:items-end sm:justify-between">
        <Link v-if="editProfileRoute" :to="editProfileRoute">
          <Button size="medium">
            <IconMdiPencil class="mr-1" />
            Edit
          </Button>
        </Link>
        <div class="flex flex-wrap gap-x-4 gap-y-1 text-sm text-gray-300 sm:justify-end">
          <span class="inline-flex items-center gap-1.5">
            <IconMdiPackageVariantClosed />
            {{ i18n.t("author.numProjects", [viewingUser.projectCount], viewingUser.projectCount) }}
          </span>
          <span class="inline-flex items-center gap-1.5">
            <IconMdiCalendarOutline />
            {{ i18n.t("author.memberSince", [i18n.d(viewingUser.createdAt, "date")]) }}
          </span>
        </div>
      </div>
    </div>
    <div v-if="viewingUser?.roles?.length" class="flex flex-wrap gap-1.5 border-t px-5 py-3 dark:border-gray-800">
      <Tag v-for="roleId in viewingUser.roles" :key="roleId" :color="{ background: getRole(roleId)?.color }" :name="getRole(roleId)?.title" />
    </div>
  </Card>
</template>

<style scoped>
.profile-hero {
  background: linear-gradient(105deg, color-mix(in srgb, var(--primary-500) 22%, transparent), transparent 62%), var(--charcoal-600);
}

.light .profile-hero {
  background: linear-gradient(105deg, color-mix(in srgb, var(--primary-500) 16%, transparent), transparent 62%), var(--gray-50);
}

.light .profile-hero .text-gray-300 {
  color: var(--gray-600);
}
</style>
