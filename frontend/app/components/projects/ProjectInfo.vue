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
      <div class="flex flex-col gap-3">
        <InfoRow :label="i18n.t('project.category.info')">
          <template #icon><CategoryLogo v-if="project" :category="project.category" :size="20" /></template>
          <template v-if="project">{{ i18n.t("project.category." + project.category) }}</template>
          <Skeleton v-else class="w-24" />
        </InfoRow>

        <InfoRow :label="i18n.t('project.info.publishDate')">
          <template #icon><IconMdiCalendar /></template>
          <span v-if="project" class="tabular-nums">{{ i18n.d(project.createdAt, "date") }}</span>
          <Skeleton v-else class="w-24" />
        </InfoRow>

        <InfoRow :label="i18n.t('project.info.license')">
          <template #icon><IconMdiLicense /></template>
          <template v-if="license">
            <Link v-if="license.url" :href="license.url" target="_blank" rel="noreferrer noopener">{{ license.label }}</Link>
            <template v-else>{{ license.label }}</template>
          </template>
          <Skeleton v-else class="w-24" />
        </InfoRow>
      </div>

      <div v-if="project" class="mt-4 flex flex-wrap gap-2 border-t border-gray-300 pt-4 dark:border-gray-700">
        <StatTile :label="i18n.t('project.info.totalDownloads', 0)" :value="project.stats.downloads">
          <template #icon><IconMdiDownload /></template>
        </StatTile>
        <StatTile :label="i18n.t('project.info.stars', 0)" :value="project.stats.stars">
          <template #icon><IconMdiStarOutline /></template>
        </StatTile>
        <StatTile v-if="hasPerms(NamedPermission.IsSubjectMember)" :label="i18n.t('project.info.views', 0)" :value="project.stats.views">
          <template #icon><IconMdiEyeOutline /></template>
        </StatTile>
      </div>

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
