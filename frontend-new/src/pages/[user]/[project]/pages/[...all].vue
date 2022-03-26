<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useContext } from "vite-ssr/vue";
import { usePage, useStaff } from "~/composables/useApiHelper";
import { useRoute, useRouter } from "vue-router";
import { useErrorRedirect } from "~/composables/useErrorRedirect";
import { User } from "hangar-api";
import { HangarProject } from "hangar-internal";
import ProjectPageList from "~/components/projects/ProjectPageList.vue";
import Markdown from "~/components/Markdown.vue";
import MarkdownEditor from "~/components/MarkdownEditor.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { ref } from "vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import Card from "~/components/design/Card.vue";
import Settings from "~/pages/[user]/[project]/settings.vue";

const props = defineProps<{
  user: User;
  project: HangarProject;
}>();

const i18n = useI18n();
const ctx = useContext();
const route = useRoute();
const router = useRouter();
const page = await usePage(route.params.user as string, route.params.project as string, route.params.all as string);
if (!page) {
  await useRouter().push(useErrorRedirect(useRoute(), 404, "Not found"));
}
const editingPage = ref(false);

async function savePage(content: string) {
  await useInternalApi(`pages/save/${props.project.id}/${page.value?.id}`, true, "post", {
    content,
  }).catch((e) => handleRequestError(e, ctx, i18n, "page.new.error.save"));
  // todo page saving
  //page.value?.contents = content;
  editingPage.value = false;
}

async function deletePage() {
  await useInternalApi(`pages/delete/${props.project.id}/${page.value?.id}`, true, "post").catch((e) =>
    handleRequestError(e, ctx, i18n, "page.new.error.save")
  );
  // todo page deleting
  //this.$refs.editor.loading.delete = false;
  await router.replace(`/${route.params.user}/${route.params.project}`);
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
          @delete="deletePage"
        />
        <Markdown v-else :raw="page.contents" />
      </Card>
    </section>
    <section class="basis-full md:basis-3/12 flex-grow">
      <ProjectPageList :project="project" />
    </section>
  </div>
</template>
