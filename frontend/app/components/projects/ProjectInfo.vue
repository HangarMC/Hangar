<script setup lang="ts">
import { NamedPermission } from "#shared/types/backend";
import type { HangarProject } from "#shared/types/backend";

const props = defineProps<{
  project?: HangarProject;
}>();
const i18n = useI18n();
const namespace = computed(() => props.project?.namespace?.owner + "/" + props.project?.name);
</script>

<template>
  <Card class="!p-0 overflow-hidden">
    <template #header>
      <div class="flex items-center gap-2 px-4 pt-3.5 pb-1">
        <h2>{{ i18n.t("project.info.title") }}</h2>
      </div>
    </template>
    <template #default>
      <div class="px-4 pt-1 pb-3">
        <div class="flex items-center gap-3 py-1.5">
          <IconMdiCalendarOutline class="flex-shrink-0 text-lg text-gray" />
          <div class="min-w-0">
            <div class="text-xs text-gray">{{ i18n.t("project.info.publishDate") }}</div>
            <div v-if="project" class="font-semibold">{{ i18n.d(project.createdAt, "date") }}</div>
            <Skeleton v-else />
          </div>
        </div>
        <div class="flex items-center gap-3 py-1.5">
          <IconMdiLicense class="flex-shrink-0 text-lg text-gray" />
          <div class="min-w-0">
            <div class="text-xs text-gray">{{ i18n.t("project.info.license") }}</div>
            <div v-if="project && (project.settings.license?.type === '(custom)' || project.settings.license?.type === 'Other')" class="truncate font-semibold">
              <Link v-if="project.settings.license.url" :href="project.settings.license.url" target="_blank" rel="noreferrer noopener">
                {{ project.settings.license.name }}
              </Link>
              <template v-else>{{ project.settings.license.name }}</template>
            </div>
            <div v-else-if="project" class="truncate font-semibold">
              <Link v-if="project.settings.license.url" :href="project.settings.license.url" target="_blank" rel="noreferrer noopener">
                {{ project.settings.license.type }}
              </Link>
              <template v-else>{{ project.settings.license.type }}</template>
            </div>
            <Skeleton v-else />
          </div>
        </div>
      </div>

      <div class="grid gap-2 border-t px-3 pt-3 -mb-2 dark:border-gray-800" :class="hasPerms(NamedPermission.IsSubjectMember) ? 'grid-cols-2' : 'grid-cols-3'">
        <div v-if="hasPerms(NamedPermission.IsSubjectMember)" class="flex min-h-17 flex-col rounded-lg bg-gray-100 p-1.5 text-center dark:bg-charcoal-500">
          <div class="flex flex-1 items-center justify-center font-semibold">
            {{ project?.stats?.views.toLocaleString("en-US") || 0 }}
          </div>
          <div class="flex items-center justify-center gap-1 text-0.65rem text-gray">
            <IconMdiEyeOutline class="flex-shrink-0" />
            <span>{{ i18n.t("project.info.views", project?.stats?.views || 0) }}</span>
          </div>
        </div>
        <div class="flex min-h-17 flex-col rounded-lg bg-gray-100 p-1.5 text-center dark:bg-charcoal-500">
          <div class="flex flex-1 items-center justify-center">
            <div class="font-semibold">
              {{ project?.stats?.downloads?.toLocaleString("en-US") || 0 }}
            </div>
          </div>

          <div class="flex items-center justify-center gap-1 text-[0.65rem] text-gray">
            <IconMdiDownloadOutline class="text-gray" />
            <span>
              {{ i18n.t("project.info.totalDownloads", project?.stats?.downloads || 0) }}
            </span>
          </div>
        </div>
        <NuxtLink
          :to="`/${namespace}/stars`"
          class="flex min-h-17 flex-col rounded-lg border border-charcoal-500 bg-gray-100 p-1.5 text-center transition-colors hover:border-gray-700 dark:bg-charcoal-500"
        >
          <div class="flex-1 flex items-center justify-center">
            <div class="font-semibold">
              {{ project?.stats?.stars?.toLocaleString("en-US") || 0 }}
            </div>
          </div>

          <div class="flex items-center justify-center gap-1 text-[0.65rem] text-gray">
            <IconMdiStarOutline class="shrink-0" />
            <span>{{ i18n.t("project.info.stars", 0) }}</span>
          </div>
        </NuxtLink>

        <NuxtLink
          :to="`/${namespace}/watchers`"
          class="flex min-h-17 flex-col rounded-lg border border-charcoal-500 bg-gray-100 p-1.5 text-center transition-colors hover:border-gray-700 dark:bg-charcoal-500"
        >
          <div class="flex-1 flex items-center justify-center">
            <div class="font-semibold">
              {{ project?.stats?.watchers?.toLocaleString("en-US") || 0 }}
            </div>
          </div>

          <div class="flex items-center justify-center gap-1 text-[0.65rem] text-gray">
            <IconMdiBellOutline class="shrink-0" />
            <span>{{ i18n.t("project.info.watchers", 0) }}</span>
          </div>
        </NuxtLink>
      </div>
    </template>
    <template #footer>
      <div class="px-4 pb-3">
        <DropdownButton v-if="project && hasPerms(NamedPermission.IsStaff)" :name="i18n.t('project.actions.adminActions')" class="mb-2">
          <DropdownItem :to="`/${namespace}/flags`">
            {{ i18n.t("project.actions.flagHistory", [project.info.flagCount ?? 0]) }}
          </DropdownItem>
          <DropdownItem :to="`/${namespace}/notes`">
            {{ i18n.t("project.actions.staffNotes", [project.info.noteCount ?? 0]) }}
          </DropdownItem>
          <DropdownItem :to="`/admin/log?authorName=${project.namespace.owner}&projectSlug=${project.namespace.slug}`">
            {{ i18n.t("project.actions.userActionLogs") }}
          </DropdownItem>
        </DropdownButton>
        <VisibilityChangerModal
          v-if="project && hasPerms(NamedPermission.SeeHidden)"
          type="project"
          :prop-visibility="project.visibility"
          :post-url="`projects/visibility/${project.projectId}`"
          class="min-h-10"
        />
        <DonationModal
          v-if="project?.settings?.donation?.enable && false"
          :donation-subject="project!.settings.donation.subject"
          :donation-target="project!.namespace.owner + '/' + project!.name"
        />
      </div>
    </template>
  </Card>
</template>
