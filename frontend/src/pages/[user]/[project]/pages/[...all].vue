<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useContext } from "vite-ssr/vue";
import { useRoute, useRouter } from "vue-router";
import { User } from "hangar-api";
import { HangarProject } from "hangar-internal";
import ProjectPageList from "~/components/projects/ProjectPageList.vue";
import Markdown from "~/components/Markdown.vue";
import MarkdownEditor from "~/components/MarkdownEditor.vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import Card from "~/lib/components/design/Card.vue";
import { useProjectPage } from "~/composables/useProjectPage";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";

const props = defineProps<{
  user: User;
  project: HangarProject;
}>();

const i18n = useI18n();
const ctx = useContext();
const route = useRoute();
const router = useRouter();

const { editingPage, open, savePage, deletePage, page } = await useProjectPage(route, router, ctx, i18n, props.project);
if (page) {
  useHead(useSeo(page.value?.name, props.project.description, route, null));
}
</script>

<template>
  <div class="flex flex-wrap md:flex-nowrap gap-4">
    <section class="basis-full md:basis-9/12 flex-grow overflow-auto">
      <Card class="p-0 overflow-clip overflow-hidden">
        <MarkdownEditor
          v-if="hasPerms(NamedPermission.EDIT_PAGE)"
          ref="editor"
          v-model:editing="editingPage"
          :raw="page.contents"
          :deletable="page.deletable"
          :saveable="true"
          :cancellable="true"
          @save="savePage"
          @delete="deletePage"
        />
        <Markdown v-else :raw="page.contents" />
      </Card>
    </section>
    <section class="basis-full md:basis-3/12 flex-grow">
      <ProjectPageList :project="project" :open="open" />
    </section>
  </div>
</template>
