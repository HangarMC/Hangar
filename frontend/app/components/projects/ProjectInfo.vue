<script setup lang="ts">
import { NamedPermission, Tag } from "#shared/types/backend";
import type { HangarProject } from "#shared/types/backend";

const props = defineProps<{
  project?: HangarProject;
}>();
const i18n = useI18n();
const namespace = computed(() => props.project?.namespace?.owner + "/" + props.project?.name);

const license = computed(() => {
  const l = props.project?.settings?.license;
  if (!l) return;
  const custom = l.type === "(custom)" || l.type === "Other";
  return { label: custom ? l.name : l.type, url: l.url };
});
</script>

<template>
  <Card>
    <template #header>
      <h2>{{ i18n.t("project.info.title") }}</h2>
    </template>
    <template #default>
      <dl class="flex flex-col gap-1.5">
        <div class="flex items-baseline justify-between gap-3">
          <dt class="inline-flex flex-shrink-0 items-center gap-1.5 text-gray-secondary">
            <CategoryLogo v-if="project" :category="project.category" :size="16" class="flex-shrink-0" /> {{ i18n.t("project.category.info") }}
          </dt>
          <dd v-if="project" class="min-w-0 truncate text-right font-semibold">{{ i18n.t("project.category." + project.category) }}</dd>
          <dd v-else class="w-24"><Skeleton /></dd>
        </div>
        <div class="flex items-baseline justify-between gap-3">
          <dt class="inline-flex flex-shrink-0 items-center gap-1.5 text-gray-secondary">
            <IconMdiCalendar class="flex-shrink-0" /> {{ i18n.t("project.info.publishDate") }}
          </dt>
          <dd v-if="project" class="text-right font-semibold tabular-nums">{{ i18n.d(project.createdAt, "date") }}</dd>
          <dd v-else class="w-24"><Skeleton /></dd>
        </div>
        <div class="flex items-baseline justify-between gap-3">
          <dt class="inline-flex flex-shrink-0 items-center gap-1.5 text-gray-secondary">
            <IconMdiLicense class="flex-shrink-0" /> {{ i18n.t("project.info.license") }}
          </dt>
          <dd v-if="license" class="min-w-0 truncate text-right font-semibold">
            <Link v-if="license.url" :href="license.url" target="_blank" rel="noreferrer noopener">{{ license.label }}</Link>
            <template v-else>{{ license.label }}</template>
          </dd>
          <dd v-else class="w-24"><Skeleton /></dd>
        </div>
      </dl>

      <!-- Stats are split off so the member-only "views" row can't reshape the block above it. -->
      <dl class="mt-3 flex flex-col gap-1.5 border-t border-gray-300 pt-3 dark:border-gray-700">
        <div class="flex items-baseline justify-between gap-3">
          <dt class="inline-flex flex-shrink-0 items-center gap-1.5 text-gray-secondary">
            <IconMdiDownload class="flex-shrink-0" /> {{ i18n.t("project.info.totalDownloads", 0) }}
          </dt>
          <dd v-if="project" class="text-right font-semibold tabular-nums">{{ project.stats.downloads.toLocaleString("en-US") }}</dd>
          <dd v-else class="w-12"><Skeleton /></dd>
        </div>
        <NuxtLink :to="`/${namespace}/stars`" class="flex items-baseline justify-between gap-3 hover:color-primary">
          <dt class="inline-flex flex-shrink-0 items-center gap-1.5 text-gray-secondary">
            <IconMdiStarOutline class="flex-shrink-0" /> {{ i18n.t("project.info.stars", 0) }}
          </dt>
          <dd v-if="project" class="text-right font-semibold tabular-nums">{{ project.stats.stars.toLocaleString("en-US") }}</dd>
          <dd v-else class="w-12"><Skeleton /></dd>
        </NuxtLink>
        <NuxtLink :to="`/${namespace}/watchers`" class="flex items-baseline justify-between gap-3 hover:color-primary">
          <dt class="inline-flex flex-shrink-0 items-center gap-1.5 text-gray-secondary">
            <IconMdiBellOutline class="flex-shrink-0" /> {{ i18n.t("project.info.watchers", 0) }}
          </dt>
          <dd v-if="project" class="text-right font-semibold tabular-nums">{{ project.stats.watchers.toLocaleString("en-US") }}</dd>
          <dd v-else class="w-12"><Skeleton /></dd>
        </NuxtLink>
        <div v-if="hasPerms(NamedPermission.IsSubjectMember)" class="flex items-baseline justify-between gap-3">
          <dt class="inline-flex flex-shrink-0 items-center gap-1.5 text-gray-secondary">
            <IconMdiEyeOutline class="flex-shrink-0" /> {{ i18n.t("project.info.views", 0) }}
          </dt>
          <dd v-if="project" class="text-right font-semibold tabular-nums">{{ project.stats.views.toLocaleString("en-US") }}</dd>
          <dd v-else class="w-12"><Skeleton /></dd>
        </div>
      </dl>

      <div v-if="project?.settings?.tags?.length" class="mt-3 flex flex-wrap gap-1 border-t border-gray-300 pt-3 dark:border-gray-700">
        <span
          v-for="tag in project.settings.tags"
          :key="tag"
          class="inline-flex items-center gap-1 rounded background-card px-1.5 py-0.5 text-xs font-semibold"
        >
          <IconMdiPuzzleOutline v-if="tag === Tag.ADDON" />
          <IconMdiBookshelf v-else-if="tag === Tag.LIBRARY" />
          <IconMdiLeaf v-else-if="tag === Tag.SUPPORTS_FOLIA" />
          {{ i18n.t("project.settings.tags." + tag + ".title") }}
        </span>
      </div>
    </template>
    <template #footer>
      <div
        v-if="project && (hasPerms(NamedPermission.IsStaff) || hasPerms(NamedPermission.SeeHidden))"
        class="flex flex-wrap gap-2 border-t border-gray-300 pt-3 dark:border-gray-700"
      >
        <DropdownButton
          v-if="hasPerms(NamedPermission.IsStaff)"
          :name="i18n.t('project.actions.adminActions')"
          button-variant="outline"
          button-tone="neutral"
          button-size="sm"
        >
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
          v-if="hasPerms(NamedPermission.SeeHidden)"
          type="project"
          variant="outline"
          tone="neutral"
          size="sm"
          :prop-visibility="project.visibility"
          :post-url="`projects/visibility/${project.projectId}`"
        />
      </div>
      <DonationModal
        v-if="project?.settings?.donation?.enable && false"
        :donation-subject="project!.settings.donation.subject"
        :donation-target="project!.namespace.owner + '/' + project!.name"
      />
    </template>
  </Card>
</template>
