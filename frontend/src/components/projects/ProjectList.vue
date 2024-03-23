<script setup lang="ts">
import type { PaginatedResultProject } from "~/types/backend";

const i18n = useI18n();

defineProps<{
  projects: PaginatedResultProject;
  resetAnchor?: Element;
}>();

const emit = defineEmits<{
  (e: "update:page", value: number): void;
}>();
function emitPageUpdate(newPage: number) {
  emit("update:page", newPage);
}
const pagination = ref();
function updatePage(newPage: number) {
  pagination.value.updatePage(newPage);
}
defineExpose({ updatePage });
</script>

<template>
  <Pagination
    v-if="projects?.result"
    ref="pagination"
    :items="projects.result"
    :server-pagination="projects.pagination"
    :reset-anchor="resetAnchor"
    @update:page="emitPageUpdate"
  >
    <template #default="{ item }">
      <ProjectCard :project="item" />
    </template>
  </Pagination>
  <div v-if="projects?.result.length === 0">{{ i18n.t("hangar.projectSearch.noProjects") }}</div>
</template>
