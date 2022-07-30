<script setup lang="ts">
import { HangarProject, HangarProjectPage } from "hangar-internal";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { useProjectPage } from "~/composables/useProjectPage";
import { useContext } from "vite-ssr";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { inject } from "vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";

const props = defineProps<{
  project: HangarProject;
}>();

const route = useRoute();
const router = useRouter();
const ctx = useContext();
const i18n = useI18n();

const updateProjectPages = inject<(pages: HangarProjectPage[]) => void>("updateProjectPages");

const { editingPage, changeEditingPage, page, savePage, deletePage } = await useProjectPage(route, router, ctx, i18n, props.project);
if (page) {
  useHead(useSeo(page.value?.name, props.project.description, route, null));
}

async function deletePageAndUpdateProject() {
  await deletePage();

  try {
    if (updateProjectPages) {
      updateProjectPages(await useInternalApi<HangarProjectPage[]>(`pages/list/${props.project.id}`, false, "get"));
    }
  } catch (e: any) {
    handleRequestError(e, ctx, i18n);
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
