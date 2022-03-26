<script lang="ts" setup>
import { User } from "hangar-api";
import Card from "~/components/design/Card.vue";
import { useI18n } from "vue-i18n";
import ProjectInfo from "~/components/projects/ProjectInfo.vue";
import { HangarProject } from "hangar-internal";
import MemberList from "~/components/projects/MemberList.vue";
import MarkdownEditor from "~/components/MarkdownEditor.vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import { usePage } from "~/composables/useApiHelper";
import { useRoute } from "vue-router";
import { useContext } from "vite-ssr/vue";
import { handleRequestError } from "~/composables/useErrorHandling";
import { ref } from "vue";
import Markdown from "~/components/Markdown.vue";
import ProjectPageList from "~/components/projects/ProjectPageList.vue";

const props = defineProps<{
  user: User;
  project: HangarProject;
}>();
const i18n = useI18n();
const route = useRoute();
const context = useContext();
const page = await usePage(route.params.user as string, route.params.project as string).catch((e) => handleRequestError(e, context, i18n));
const editingPage = ref(false);

function savePage() {
  // TODO mixin?
}
</script>

<template>
  <div class="flex flex-wrap md:flex-nowrap gap-4">
    <section class="basis-full md:basis-9/12 flex-grow">
      <Card class="p-0">
        <MarkdownEditor
          v-if="hasPerms(NamedPermission.EDIT_PAGE)"
          ref="editor"
          v-model:editing="editingPage"
          :raw="page.contents"
          :deletable="page.deletable"
          @save="savePage"
        />
        <Markdown v-else :raw="page.contents" />
      </Card>
    </section>
    <section class="basis-full md:basis-3/12 flex-grow space-y-4">
      <ProjectInfo :project="project"></ProjectInfo>
      <Card>
        <template #header>{{ i18n.t("project.promotedVersions") }}</template>
        <template #default>Promoted versions go here</template>
      </Card>
      <ProjectPageList :project="project" />
      <MemberList :project="project" />
    </section>
  </div>
</template>
