<script setup lang="ts">
import type { ExtendedProjectPage, HangarProject, HangarProjectPage } from "~/types/backend";

const props = defineProps<{
  project: HangarProject;
  mainPage: boolean;
}>();

const route = useRoute("user-project-pages-all");
const router = useRouter();

const updateProjectPages = inject<(pages: HangarProjectPage[]) => void>("updateProjectPages");

const { editingPage, changeEditingPage, page, savePage, deletePage } = await useProjectPage(route, router, props.project, props.mainPage);
if (page && !props.mainPage) {
  useHead(useSeo(page.value?.name + " | " + props.project.name, props.project.description, route, props.project.avatarUrl));
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

defineSlots<{
  default: (props: {
    page: ExtendedProjectPage | HangarProjectPage | null;
    editingPage: boolean;
    changeEditingPage: (editing: boolean) => void;
    savePage: (content: string) => void;
    deletePage: () => void;
  }) => any;
}>();
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
