<script setup lang="ts">
import type { PaginatedResultProject, ProjectCompact } from "#shared/types/backend";

const i18n = useI18n();
const showSkeletons = ref(false);
let loadingTimeout: ReturnType<typeof setTimeout> | null;

const props = defineProps<{
  projects?: PaginatedResultProject;
  resetAnchor?: HTMLElement | null;
  loading?: boolean;
  canEdit?: boolean;
  pinned?: ProjectCompact[];
}>();

const emit = defineEmits<{
  (e: "update:page", value: number): void;
}>();
function emitPageUpdate(newPage: number) {
  emit("update:page", newPage);
}
const pagination = useTemplateRef("pagination");
function updatePage(newPage: number) {
  pagination.value?.updatePage(newPage);
}
defineExpose({ updatePage });

watch(() => props.loading, (isLoading) => {
  if (isLoading) {
    loadingTimeout = setTimeout(() => {
      showSkeletons.value = true;
    }, 500);
  } else {
    showSkeletons.value = false;
    if (loadingTimeout) {
      clearTimeout(loadingTimeout);
    }
  }
});
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
      <Transition name="list" appear>
        <ProjectCard class="hover:scale-[1.005] transition-all duration-200" :project="item" :can-edit :pinned="pinned?.some((p) => p.namespace.slug === item.namespace.slug)" />
      </Transition>
    </template>
  </Pagination>
  <div v-if="projects?.result?.length === 0">{{ i18n.t("hangar.projectSearch.noProjects") }}</div>
  <template v-if="showSkeletons">
    <Skeleton v-for="n in 10" :key="n" class="h-40 rounded-xl" />
  </template>
</template>
