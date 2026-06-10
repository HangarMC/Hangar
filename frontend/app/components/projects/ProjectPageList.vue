<script setup lang="ts">
import { NamedPermission } from "#shared/types/backend";
import type { HangarProject } from "#shared/types/backend";

defineProps<{
  project?: HangarProject;
  open: string[];
}>();

const i18n = useI18n();
const route = useRoute("user-project");

function countPages(pages: HangarProject["pages"] = []): number {
  return pages.reduce((count, page) => count + 1 + countPages(page.children), 0);
}
</script>

<template>
  <Card class="!p-0 overflow-hidden">
    <template #header>
      <div class="flex w-full items-center gap-2 px-4 pt-3.5 pb-2.5">
        <IconMdiFileTreeOutline class="color-primary" />
        <h2>{{ i18n.t("page.plural") }}</h2>
        <span v-if="project" class="rounded-full bg-gray-100 px-2 py-0.5 text-xs font-normal text-gray dark:bg-charcoal-500">
          {{ countPages(project.pages) }}
        </span>
        <div class="flex-grow" />
        <NewPageModal v-if="project && hasPerms(NamedPermission.EditPage)" :pages="project.pages" :project-id="project.id" />
      </div>
    </template>

    <div class="border-t p-3 dark:border-gray-800">
      <div v-if="project" class="rounded-lg border border-gray-200 bg-gray-100/60 px-2 py-1.5 dark:border-gray-800 dark:bg-charcoal-500/60">
        <TreeView :items="project.pages" item-key="slug" :open="open" clazz="py-1">
          <template #item="{ item }">
            <Link
              v-if="item.home"
              :to="`/${route.params.user}/${route.params.project}`"
              exact
              class="inline-flex min-w-0 items-center gap-1.5 rounded-md px-1.5 py-1"
              active-underline
            >
              <IconMdiHomeOutline class="flex-shrink-0 color-primary" />
              <span class="truncate">{{ item.name }}</span>
            </Link>
            <Link
              v-else
              :to="`/${route.params.user}/${route.params.project}/pages/${item.slug}`"
              exact
              class="min-w-0 truncate rounded-md px-1.5 py-1"
              active-underline
            >
              {{ item.name }}
            </Link>
          </template>
        </TreeView>
      </div>
      <Skeleton v-else />
    </div>
  </Card>
</template>
