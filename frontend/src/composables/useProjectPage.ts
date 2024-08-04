import type { HangarProject } from "~/types/backend";
import type { Router } from "vue-router";
import type { RouteLocationNormalized } from "vue-router/auto";

export async function useProjectPage(route: RouteLocationNormalized<"user-project-pages-all">, router: Router, project?: HangarProject, mainPage?: boolean) {
  const page = mainPage ? computed(() => project?.mainPage) : usePage(() => ({ project: route.params.project, path: route.params.all?.toString() })).page;
  // TODO fix this
  // if (!page?.value) {
  //   throw useErrorRedirect(404, "Not found");
  // }

  const editingPage = ref<boolean>(false);

  // Helper setter function, v-model cannot directly edit from inside a slot.
  function changeEditingPage(newValue: boolean) {
    editingPage.value = newValue;
  }

  async function savePage(content: string) {
    if (!page?.value) return;
    await useInternalApi(`pages/save/${project?.id}/${page.value?.id}`, "post", {
      content,
    }).catch((e) => handleRequestError(e, "page.new.error.save"));
    if (page.value && "contents" in page.value) {
      page.value.contents = content;
    }
    editingPage.value = false;
  }

  async function deletePage() {
    if (!page?.value) return;
    await useInternalApi(`pages/delete/${project?.id}/${page.value?.id}`, "post").catch((e) => handleRequestError(e, "page.new.error.save"));
    await router.replace(`/${route.params.user}/${route.params.project}`);
  }

  return { editingPage, changeEditingPage, page, savePage, deletePage };
}
