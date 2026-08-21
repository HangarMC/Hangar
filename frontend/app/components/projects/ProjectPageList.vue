<script setup lang="ts">
import { NamedPermission } from "#shared/types/backend";
import type { HangarProject } from "#shared/types/backend";

defineProps<{
  project?: HangarProject;
  open: string[];
}>();

const i18n = useI18n();
const route = useRoute("user-project");
</script>

<template>
  <Card>
    <template #header>
      <div class="flex w-full items-center justify-between gap-2">
        <h2>{{ i18n.t("page.plural") }}</h2>
        <NewPageModal v-if="project && hasPerms(NamedPermission.EditPage)" :pages="project.pages" :project-id="project.id" />
      </div>
    </template>

    <TreeView v-if="project" :items="project.pages || []" item-key="slug" :open="open">
      <template #item="{ item }">
        <NuxtLink
          :to="item.home ? `/${route.params.user}/${route.params.project}` : `/${route.params.user}/${route.params.project}/pages/${item.slug}`"
          exact-active-class="page-link-active"
          class="page-link min-w-0 flex flex-1 items-center gap-2 rounded px-2 py-1.5 transition-colors"
        >
          <IconMdiHome v-if="item.home" class="flex-shrink-0 text-gray-secondary" />
          <IconMdiFileDocumentOutline v-else class="flex-shrink-0 text-gray-secondary" />
          <span class="min-w-0 truncate">{{ item.name }}</span>
        </NuxtLink>
      </template>
    </TreeView>
    <div v-else class="flex flex-col gap-2">
      <Skeleton />
      <Skeleton />
    </div>
  </Card>
</template>

<style scoped>
.page-link:hover {
  @apply background-card;
}

.page-link-active.page-link-active {
  @apply color-primary font-semibold;
}

.page-link-active :deep(svg),
.page-link:hover :deep(svg) {
  color: inherit;
}
</style>
