<script setup lang="ts">
import type { HangarProject, HangarProjectPage, ProjectPageTable } from "~/types/backend";

const props = defineProps<{
  project?: HangarProject;
  page?: ProjectPageTable;
  mainPage: boolean;
}>();

const route = useRoute("user-project-pages-page");
const router = useRouter();

const updateProjectPages = inject<(pages: HangarProjectPage[]) => void>("updateProjectPages");

const { editingPage, changeEditingPage, savePage, deletePage } = useProjectPage(
  () => route,
  router,
  () => props.project,
  () => (props.mainPage ? props.project?.mainPage : props.page)
);
if (!props.mainPage) {
  useSeo(
    computed(() => ({ title: props.page?.name + " | " + props.project?.name, description: props.project?.description, route, image: props.project?.avatarUrl }))
  );
}

async function deletePageAndUpdateProject() {
  await deletePage();

  try {
    if (updateProjectPages) {
      updateProjectPages(await useInternalApi<HangarProjectPage[]>(`pages/list/${props.project?.id}`, "get"));
    }
  } catch (err: any) {
    handleRequestError(err);
  }
}

defineSlots<{
  default: (props: { editingPage: boolean; changeEditingPage: (editing: boolean) => void; savePage: (content: string) => void; deletePage: () => void }) => any;
}>();
</script>

<template>
  <slot :save-page="savePage" :delete-page="deletePageAndUpdateProject" :editing-page="editingPage" :change-editing-page="changeEditingPage" />
</template>
