<script setup lang="ts">
import { PaginatedResult, Project } from "hangar-api";
import { PropType } from "vue";
import Card from "~/components/design/Card.vue";
import Link from "~/components/design/Link.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { useI18n } from "vue-i18n";

const i18n = useI18n();

const props = defineProps({
  projects: {
    type: Object as PropType<PaginatedResult<Project>>,
    required: true,
  },
});
</script>

<template>
  <Card v-for="project in projects?.result" :key="project.name" class="flex mb-2 space-x-4">
    <div>
      <UserAvatar
        :username="project.namespace.owner"
        :to="'/' + project.namespace.owner + '/' + project.name"
        :img-src="projectIconUrl(project.namespace.owner, project.name)"
        size="md"
      />
    </div>
    <div>
      <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">{{ project.name }}</Link>
      by
      <Link :to="'/' + project.namespace.owner">{{ project.namespace.owner }}</Link>
      <br />
      {{ project.description }}
    </div>
    <div class="flex-grow"></div>
    <div class="flex flex-col">
      <span class="inline-flex items-center"
        ><IconMdiEye class="mx-1" /> {{ project.stats.views }} {{ i18n.t("project.info.views", project.stats.views) }}</span
      >
      <span class="inline-flex items-center"
        ><IconMdiDownload class="mx-1" /> {{ project.stats.downloads }} {{ i18n.t("project.info.totalDownloads", project.stats.downloads) }}</span
      >
      <span class="inline-flex items-center"
        ><IconMdiStar class="mx-1" /> {{ project.stats.stars }} {{ i18n.t("project.info.stars", project.stats.stars) }}</span
      >
    </div>
  </Card>
  <div v-if="projects?.result.length === 0">{{ i18n.t("hangar.projectSearch.noProjects") }}</div>
</template>
