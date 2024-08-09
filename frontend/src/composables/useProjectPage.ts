import type { ExtendedProjectPage, HangarProject } from "~/types/backend";
import type { Router } from "vue-router";
import type { RouteLocationNormalized } from "vue-router/auto";

export function useProjectPage(route: RouteLocationNormalized<"user-project-pages-page">, router: Router, project?: HangarProject, page?: ExtendedProjectPage) {
  const editingPage = ref<boolean>(false);

  // Helper setter function, v-model cannot directly edit from inside a slot.
  function changeEditingPage(newValue: boolean) {
    editingPage.value = newValue;
  }

  async function savePage(content: string) {
    if (!page) return;
    await useInternalApi(`pages/save/${project?.id}/${page?.id}`, "post", {
      content,
    }).catch((e) => handleRequestError(e, "page.new.error.save"));
    if (page && "contents" in page) {
      page.contents = content;
    }
    editingPage.value = false;
  }

  async function deletePage() {
    if (!page) return;
    await useInternalApi(`pages/delete/${project?.id}/${page?.id}`, "post").catch((e) => handleRequestError(e, "page.new.error.save"));
    await router.replace(`/${route.params.user}/${route.params.project}`);
  }

  return { editingPage, changeEditingPage, savePage, deletePage };
}
