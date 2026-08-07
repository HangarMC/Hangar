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

const socialLinks = computed(() => {
  const socials = props.viewingUser?.socials;
  if (!socials) return [];
  return [
    { key: "github", label: "GitHub", href: socials.github ? `https://github.com/${socials.github}` : undefined },
    { key: "twitter", label: "Twitter", href: socials.twitter ? `https://twitter.com/${socials.twitter}` : undefined },
    { key: "youtube", label: "YouTube", href: socials.youtube ? `https://youtube.com/${socials.youtube}` : undefined },
    { key: "website", label: "Website", href: socials.website ? linkout(socials.website) : undefined },
  ].filter((link) => link.href);
});
</script>

<template>
  <Card accent class="overflow-visible">
    <div class="flex flex-wrap items-start gap-4">
      <div class="flex-shrink-0">
        <EditableAvatar
          v-if="viewingUser && hasPerms(NamedPermission.EditSubjectSettings)"
          :username="viewingUser.name"
          :avatar-url="viewingUser.avatarUrl"
          :action="`${viewingUser.isOrganization ? 'organizations/org' : 'users'}/${viewingUser.name}/settings/avatar`"
          compact
        />
        <UserAvatar v-else :username="viewingUser?.name" :avatar-url="viewingUser?.avatarUrl" :loading="!viewingUser" />
      </div>

      <div class="min-w-0 flex-1">
        <template v-if="viewingUser">
          <div class="flex flex-wrap items-center gap-x-2 gap-y-1">
            <h1 class="min-w-0 truncate text-2xl text-strong">{{ viewingUser.name }}</h1>

            <Tooltip v-if="viewingUser.locked">
              <template #content>{{ i18n.t("author.tooltips.lock") }}</template>
              <IconMdiLockOutline class="flex-shrink-0 text-gray-secondary" />
            </Tooltip>

            <Popper v-if="viewingUser.nameHistory?.length > 0" placement="bottom">
              <button
                type="button"
                class="inline-flex flex-shrink-0 items-center rounded text-gray-secondary hover:color-primary"
                :aria-label="i18n.t('author.nameHistory')"
                :title="i18n.t('author.nameHistory')"
              >
                <IconMdiHistory />
              </button>
              <template #content>
                <DropdownPanel class="p-3 text-base">
                  <div class="font-bold">{{ i18n.t("author.nameHistory") }}</div>
                  <div v-for="(history, idx) of viewingUser.nameHistory" :key="idx" class="text-sm text-gray-secondary">
                    {{ history.oldName }} until <PrettyTime :time="history.date" long />
                  </div>
                </DropdownPanel>
              </template>
            </Popper>
          </div>

          <div class="mt-0.5">
            <TaglineModal
              v-if="canEditCurrentUser"
              :tagline="viewingUser.tagline"
              :action="`${viewingUser.isOrganization ? 'organizations/org' : 'users'}/${viewingUser.name}/settings/tagline`"
            >
              <template #activator="{ on }">
                <button
                  type="button"
                  class="-ml-1 inline-flex max-w-full items-center gap-1 rounded px-1 py-0.5 text-left text-gray-secondary hover:background-card focus-visible:(outline-2 outline-primary-500)"
                  :aria-label="i18n.t('author.editTagline')"
                  v-on="on"
                >
                  <span class="truncate">{{ viewingUser.tagline || i18n.t("author.addTagline") }}</span>
                  <IconMdiPencil class="shrink-0" />
                </button>
              </template>
            </TaglineModal>
            <span v-else-if="viewingUser.tagline" class="text-gray-secondary">{{ viewingUser.tagline }}</span>
          </div>

          <div v-if="socialLinks.length > 0 || viewingUser.socials?.discord || canEditCurrentUser" class="mt-2 flex flex-wrap items-center gap-1">
            <a
              v-for="link in socialLinks"
              :key="link.key"
              :href="link.href"
              class="inline-flex items-center rounded p-1 text-lg text-gray-secondary transition-colors hover:color-primary"
              rel="external nofollow"
              :title="link.label"
              :aria-label="link.label"
            >
              <IconMdiGithub v-if="link.key === 'github'" />
              <IconMdiTwitter v-else-if="link.key === 'twitter'" />
              <IconMdiYoutube v-else-if="link.key === 'youtube'" />
              <IconMdiWeb v-else />
            </a>
            <Tooltip v-if="viewingUser.socials?.discord">
              <template #content>
                <span class="text-base">{{ viewingUser.socials.discord }}</span>
              </template>
              <span class="inline-flex items-center rounded p-1 text-lg text-gray-secondary"><IconMdiDiscord /></span>
            </Tooltip>
            <SocialsModal
              v-if="canEditCurrentUser"
              :socials="viewingUser.socials"
              :action="`${viewingUser.isOrganization ? 'organizations/org' : 'users'}/${viewingUser.name}/settings/socials`"
            />
          </div>
        </template>
        <template v-else>
          <Skeleton class="w-50 text-2xl" />
          <Skeleton class="mt-1 w-100" />
        </template>
      </div>

      <div class="flex flex-col gap-1.5 lt-md:(mt-1 basis-full items-start) md:(ml-auto flex-shrink-0 items-end)">
        <template v-if="viewingUser">
          <span class="inline-flex items-center gap-1.5 text-sm text-gray-secondary">
            <IconMdiCalendar class="flex-shrink-0" />
            {{ i18n.t("author.memberSince", [i18n.d(viewingUser.createdAt, "date")]) }}
          </span>
          <span class="inline-flex items-center gap-1.5 text-sm text-gray-secondary tabular-nums">
            <IconMdiPackageVariantClosed class="flex-shrink-0" />
            {{ i18n.t("author.numProjects", [viewingUser.projectCount], viewingUser.projectCount) }}
          </span>
          <div v-if="viewingUser.roles?.length" class="flex flex-wrap gap-1 md:justify-end">
            <Tag v-for="roleId in viewingUser.roles" :key="roleId" :color="{ background: getRole(roleId)?.color }" :name="getRole(roleId)?.title" />
          </div>
        </template>
        <template v-else>
          <Skeleton class="w-40" />
          <Skeleton class="w-40" />
        </template>
      </div>
    </div>
  </Card>
  <hr class="my-4 border-gray-300 dark:border-gray-700" />
</template>
