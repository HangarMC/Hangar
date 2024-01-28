<script lang="ts" setup>
import type { User } from "hangar-api";
import type { HangarProject } from "hangar-internal";
import { NamedPermission } from "~/types/enums";

const props = defineProps<{
  user: User;
  project: HangarProject;
}>();

const route = useRoute<"user-project-pages-all">();

const open = await useOpenProjectPages(route, props.project);
// useSeo is in ProjectPageMarkdown
</script>

<template>
  <div class="flex flex-wrap md:flex-nowrap gap-4">
    <section class="basis-full md:basis-9/12 flex-grow overflow-auto">
      <ProjectPageMarkdown
        :key="route.fullPath"
        v-slot="{ page, editingPage, changeEditingPage, savePage, deletePage }"
        :project="props.project"
        :main-page="false"
      >
        <Card v-if="page" class="pb-0 overflow-clip overflow-hidden">
          <ClientOnly v-if="hasPerms(NamedPermission.EDIT_PAGE)">
            <MarkdownEditor
              ref="editor"
              :editing="editingPage"
              :raw="page.contents"
              :deletable="page.deletable"
              :saveable="true"
              :cancellable="true"
              @update:editing="changeEditingPage"
              @save="savePage"
              @delete="deletePage"
            />
            <template #fallback>
              <Markdown :raw="page.contents" />
            </template>
          </ClientOnly>
          <Markdown v-else :raw="page.contents" />
        </Card>
      </ProjectPageMarkdown>
      <!--We have to blow up v-model:editing into :editing and @update:editing as we are inside a scope--->
    </section>
    <section class="basis-full md:basis-3/12 flex-grow">
      <ProjectPageList :project="project" :open="open" />
    </section>
  </div>
</template>
