import type { HangarProject } from "hangar-internal";
import type { RouteLocationNormalizedLoaded, Router } from "vue-router";
import type { RouteLocationNormalizedTyped } from "unplugin-vue-router";

export async function useProjectPage(route: RouteLocationNormalizedTyped<"user-project-pages-all">, router: Router, project: HangarProject, mainPage: boolean) {
  const page = mainPage ? ref(project.mainPage) : await usePage(route.params.project, route.params.all);
  if (!page?.value) {
    throw useErrorRedirect(route, 404, "Not found");
  }

  const editingPage = ref<boolean>(false);

  // Helper setter function, v-model cannot directly edit from inside a slot.
  function changeEditingPage(newValue: boolean) {
    editingPage.value = newValue;
  }

  async function savePage(content: string) {
    if (!page?.value) return;
    await useInternalApi(`pages/save/${project.id}/${page.value?.id}`, "post", {
      content,
    }).catch((e) => handleRequestError(e, "page.new.error.save"));
    if (page.value) {
      page.value.contents = content;
    }
    editingPage.value = false;
  }

  async function deletePage() {
    if (!page) return;
    await useInternalApi(`pages/delete/${project.id}/${page.value?.id}`, "post").catch((e) => handleRequestError(e, "page.new.error.save"));
    await router.replace(`/${route.params.user}/${route.params.project}`);
  }

  return { editingPage, changeEditingPage, page, savePage, deletePage };
}
