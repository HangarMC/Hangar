<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { HangarProject } from "hangar-internal";
import { forumUrl } from "~/composables/useUrlHelper";
import Card from "~/lib/components/design/Card.vue";
import Link from "~/lib/components/design/Link.vue";
import DropdownButton from "~/lib/components/design/DropdownButton.vue";
import DropdownItem from "~/lib/components/design/DropdownItem.vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import DonationModal from "~/components/donation/DonationModal.vue";
import VisibilityChangerModal from "~/components/modals/VisibilityChangerModal.vue";
import ComingSoon from "~/lib/components/design/ComingSoon.vue";

const props = defineProps<{
  project: HangarProject;
}>();
const i18n = useI18n();
const slug = computed(() => props.project.namespace.owner + "/" + props.project.name);
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
            <td class="text-right">{{ i18n.t("project.category." + project.category) }}</td>
          </tr>
          <tr>
            <th class="text-left">{{ i18n.t("project.info.publishDate") }}</th>
            <td class="text-right">{{ i18n.d(project.createdAt, "date") }}</td>
          </tr>
          <tr>
            <th class="text-left">{{ i18n.t("project.info.views", 0) }}</th>
            <td class="text-right">
              <ComingSoon>{{ project.stats.views }}</ComingSoon>
            </td>
          </tr>
          <tr>
            <th class="text-left">{{ i18n.t("project.info.totalDownloads", 0) }}</th>
            <td class="text-right">
              <ComingSoon>{{ project.stats.downloads }}</ComingSoon>
            </td>
          </tr>
          <tr>
            <th class="text-left">
              <Link :to="`/${slug}/stars`">
                {{ i18n.t("project.info.stars", 0) }}
              </Link>
            </th>
            <td class="text-right">{{ project.stats.stars }}</td>
          </tr>
          <tr>
            <th class="text-left">
              <Link :to="`/${slug}/watchers`">
                {{ i18n.t("project.info.watchers", 0) }}
              </Link>
            </th>
            <td class="text-right">{{ project.stats.watchers }}</td>
          </tr>
        </tbody>
      </table>
    </template>
    <template #footer>
      <DropdownButton v-if="hasPerms(NamedPermission.IS_STAFF)" :name="i18n.t('project.actions.adminActions')" class="mb-2">
        <DropdownItem :to="`/${slug}/flags`">
          {{ i18n.t("project.actions.flagHistory", [project.info.flagCount]) }}
        </DropdownItem>
        <DropdownItem :to="`/${slug}/notes`">
          {{ i18n.t("project.actions.staffNotes", [project.info.noteCount]) }}
        </DropdownItem>
        <DropdownItem :to="`/admin/log/?projectFilter=/${slug}`">
          {{ i18n.t("project.actions.userActionLogs") }}
        </DropdownItem>
        <DropdownItem v-if="project.topicId" :href="forumUrl(project.topicId)">
          {{ i18n.t("project.actions.forum") }}
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
