<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { HangarProject } from "hangar-internal";
import { forumUrl } from "~/composables/useUrlHelper";
import Card from "~/components/design/Card.vue";
import Link from "~/components/design/Link.vue";
import DropdownButton from "~/components/design/DropdownButton.vue";
import DropdownItem from "~/components/design/DropdownItem.vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission, Tag } from "~/types/enums";
import DonationModal from "~/components/donation/DonationModal.vue";
import VisibilityChangerModal from "~/components/modals/VisibilityChangerModal.vue";

const props = defineProps<{
  project: HangarProject;
}>();
const i18n = useI18n();
const namespace = computed(() => props.project.namespace.owner + "/" + props.project.name);
</script>

<template>
  <Card>
    <template #header>
      <h3>{{ i18n.t("project.info.title") }}</h3>
    </template>
    <template #default>
      <table class="w-full">
        <tbody>
          <tr>
            <th class="text-left">{{ i18n.t("project.category.info") }}</th>
            <td>{{ i18n.t("project.category." + project.category) }}</td>
          </tr>
          <tr>
            <th class="text-left">{{ i18n.t("project.info.publishDate") }}</th>
            <td>{{ i18n.d(project.createdAt, "date") }}</td>
          </tr>
          <tr>
            <th class="text-left">{{ i18n.t("project.info.license") }}</th>
            <td v-if="project.settings.license?.type === '(custom)' || project.settings.license?.type === 'Other'">
              <Link v-if="project.settings.license.url" :href="project.settings.license.url" target="_blank" rel="noreferrer noopener">
                {{ project.settings.license.name }}
              </Link>
              <template v-else>
                {{ project.settings.license.name }}
              </template>
            </td>
            <td v-else>
              <Link v-if="project.settings.license.url" :href="project.settings.license.url" target="_blank" rel="noreferrer noopener">
                {{ project.settings.license.type }}
              </Link>
              <template v-else>
                {{ project.settings.license.type }}
              </template>
            </td>
          </tr>
          <tr v-if="hasPerms(NamedPermission.IS_SUBJECT_MEMBER)">
            <th class="text-left">{{ i18n.t("project.info.views", project.stats.views) }}</th>
            <td>
              {{ project.stats.views.toLocaleString("en-US") }}
            </td>
          </tr>
          <tr>
            <th class="text-left">{{ i18n.t("project.info.totalDownloads", project.stats.downloads) }}</th>
            <td>
              {{ project.stats.downloads.toLocaleString("en-US") }}
            </td>
          </tr>
          <tr>
            <th class="text-left">
              <Link :to="`/${namespace}/stars`">
                {{ i18n.t("project.info.stars", 0) }}
              </Link>
            </th>
            <td>{{ project.stats.stars.toLocaleString("en-US") }}</td>
          </tr>
          <tr>
            <th class="text-left">
              <Link :to="`/${namespace}/watchers`">
                {{ i18n.t("project.info.watchers", 0) }}
              </Link>
            </th>
            <td>{{ project.stats.watchers.toLocaleString("en-US") }}</td>
          </tr>
        </tbody>
      </table>

      <div v-for="tag in project.settings.tags" :key="tag">
        <div class="inline-flex items-center">
          <IconMdiPuzzleOutline v-if="tag === Tag.ADDON" />
          <IconMdiBookshelf v-else-if="tag === Tag.LIBRARY" />
          <IconMdiLeaf v-else-if="tag === Tag.SUPPORTS_FOLIA" />
          <span class="ml-1">{{ i18n.t("project.settings.tags." + tag + ".title") }}</span>
        </div>
      </div>
    </template>
    <template #footer>
      <DropdownButton v-if="hasPerms(NamedPermission.IS_STAFF)" :name="i18n.t('project.actions.adminActions')" class="mb-2">
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
        v-if="hasPerms(NamedPermission.SEE_HIDDEN)"
        type="project"
        :prop-visibility="project.visibility"
        :post-url="`projects/visibility/${project.projectId}`"
        class="min-h-10"
      />
      <DonationModal
        v-if="project.settings.donation.enable && false"
        :donation-subject="project.settings.donation.subject"
        :donation-target="project.namespace.owner + '/' + project.name"
      />
    </template>
  </Card>
</template>
