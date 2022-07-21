<script setup lang="ts">
import { HangarProject } from "hangar-internal";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { useProjectPage } from "~/composables/useProjectPage";
import { useContext } from "vite-ssr";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";

const props = defineProps<{
  project: HangarProject;
}>();

const route = useRoute();
const router = useRouter();
const ctx = useContext();
const i18n = useI18n();

const { editingPage, changeEditingPage, page, savePage, deletePage } = await useProjectPage(route, router, ctx, i18n, props.project);
if (page) {
  useHead(useSeo(page.value?.name, props.project.description, route, null));
}
</script>

<template>
  <slot :page="page" :save-page="savePage" :delete-page="deletePage" :editing-page="editingPage" :change-editing-page="changeEditingPage"></slot>
</template>
