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
      <div class="inline-flex w-full flex-cols space-between">
        <h2 class="flex-grow">{{ i18n.t("page.plural") }}</h2>
        <NewPageModal v-if="project && hasPerms(NamedPermission.EditPage)" :pages="project.pages" :project-id="project.id" activator-class="mr-2" />
      </div>
    </template>

    <TreeView :items="project?.pages || []" item-key="slug" :open="open">
      <template #item="{ item }">
        <Link v-if="item.home" :to="`/${route.params.user}/${route.params.project}`" exact class="inline-flex items-center" active-underline>
          <IconMdiHome class="mr-1" />
          {{ item.name }}
        </Link>
        <Link v-else :to="`/${route.params.user}/${route.params.project}/pages/${item.slug}`" exact active-underline> {{ item.name }}</Link>
      </template>
    </TreeView>
    <Skeleton v-if="!project" />
  </Card>
</template>
