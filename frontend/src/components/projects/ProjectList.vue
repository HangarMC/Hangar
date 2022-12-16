<script setup lang="ts">
import { PaginatedResult, Project } from "hangar-api";
import { useI18n } from "vue-i18n";
import Pagination from "~/lib/components/design/Pagination.vue";
import ProjectCard from "~/components/projects/ProjectCard.vue";

const i18n = useI18n();

const props = defineProps<{
  projects: PaginatedResult<Project>;
}>();

const emit = defineEmits<{
  (e: "update:page", value: number): void;
}>();
function updatePage(newPage: number) {
  emit("update:page", newPage);
}
</script>

<template>
  <Pagination v-if="projects?.result" :items="projects.result" :server-pagination="projects.pagination" @update:page="updatePage">
    <template #default="{ item }">
      <ProjectCard :project="item"></ProjectCard>
    </template>
  </Pagination>
  <div v-if="projects?.result.length === 0">{{ i18n.t("hangar.projectSearch.noProjects") }}</div>
</template>
