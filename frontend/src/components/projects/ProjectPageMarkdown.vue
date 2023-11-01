<script setup lang="ts">
import type { HangarProject, HangarProjectPage } from "hangar-internal";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { useHead } from "@unhead/vue";
import { inject } from "vue";
import { useProjectPage } from "~/composables/useProjectPage";
import { useSeo } from "~/composables/useSeo";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";

const props = defineProps<{
  project: HangarProject;
  mainPage: boolean;
}>();

const route = useRoute();
const router = useRouter();
const i18n = useI18n();

const updateProjectPages = inject<(pages: HangarProjectPage[]) => void>("updateProjectPages");

const { editingPage, changeEditingPage, page, savePage, deletePage } = await useProjectPage(route, router, props.project, props.mainPage);
if (page) {
  const title = props.mainPage ? props.project.name : page.value?.name + " | " + props.project.name;
  useHead(useSeo(title, props.project.description, route, props.project.avatarUrl));
}

async function deletePageAndUpdateProject() {
  await deletePage();

  try {
    if (updateProjectPages) {
      updateProjectPages(await useInternalApi<HangarProjectPage[]>(`pages/list/${props.project.id}`, "get"));
    }
  } catch (e: any) {
    handleRequestError(e);
  }
}
</script>

<template>
  <slot
    :page="page"
    :save-page="savePage"
    :delete-page="deletePageAndUpdateProject"
    :editing-page="editingPage"
    :change-editing-page="changeEditingPage"
  ></slot>
</template>
