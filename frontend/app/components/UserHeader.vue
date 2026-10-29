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
    { key: "website", label: "Website", href: socials.website ? linkout(socials.website) : undefined },
  ].filter((link) => link.href);
});

const hasSocials = computed(() => socialLinks.value.length > 0 || Boolean(props.viewingUser?.socials?.discord));

const roles = computed(() => displayRoles(props.viewingUser?.roles));
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

            <Chip v-if="viewingUser.isOrganization">
              <IconMdiAccountGroupOutline />
              {{ i18n.t("author.organizationLabel") }}
            </Chip>

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

            <div v-if="roles.length > 0" class="flex flex-wrap gap-1">
              <Tag v-for="role in roles" :key="role.roleId" :color="{ background: role.color }" :name="role.title" />
            </div>

            <div v-if="hasSocials || canEditCurrentUser" class="flex flex-wrap items-center gap-1">
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
                :label="hasSocials ? undefined : i18n.t('author.addSocials')"
                :action="`${viewingUser.isOrganization ? 'organizations/org' : 'users'}/${viewingUser.name}/settings/socials`"
              />
            </div>
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
        </template>
        <template v-else>
          <Skeleton class="w-50 text-2xl" />
          <Skeleton class="mt-1 w-100" />
        </template>
      </div>

      <div class="flex gap-2 lt-md:(mt-1 basis-full) md:(ml-auto w-72 flex-shrink-0)">
        <template v-if="viewingUser">
          <StatTile
            :label="viewingUser.isOrganization ? i18n.t('author.createdLabel') : i18n.t('author.memberSinceLabel')"
            :value="i18n.d(viewingUser.createdAt, 'shortdate')"
          >
            <template #icon><IconMdiCalendar /></template>
          </StatTile>
          <StatTile :label="i18n.t('author.numProjectsLabel')" :value="viewingUser.projectCount">
            <template #icon><IconMdiPackageVariantClosed /></template>
          </StatTile>
        </template>
        <template v-else>
          <Skeleton class="h-16 flex-1" />
          <Skeleton class="h-16 flex-1" />
        </template>
      </div>
    </div>
  </Card>
  <hr class="my-4 border-gray-300 dark:border-gray-700" />
</template>
