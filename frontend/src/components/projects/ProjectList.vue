<script setup lang="ts">
import { PaginatedResult, Project } from "hangar-api";
import { PropType } from "vue";
import { useI18n } from "vue-i18n";
import Pagination from "~/lib/components/design/Pagination.vue";
import ProjectCard from "~/components/projects/ProjectCard.vue";

const i18n = useI18n();

const props = defineProps({
  projects: {
    type: Object as PropType<PaginatedResult<Project>>,
    required: true,
  },
});
</script>

<template>
  <Pagination :items="projects?.result">
    <template #default="{ item }">
      <ProjectCard :project="item"></ProjectCard>
    </template>
  </Pagination>
  <div v-if="projects?.result.length === 0">{{ i18n.t("hangar.projectSearch.noProjects") }}</div>
</template>
