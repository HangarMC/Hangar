import type { Router, RouteLocationNormalized } from "vue-router";
import type { HangarProject, ProjectPageTable } from "#shared/types/backend";

export function useProjectPage(
  route: MaybeRefOrGetter<RouteLocationNormalized<"user-project-pages-page">>,
  router: Router,
  project: MaybeRefOrGetter<HangarProject | undefined>,
  page: MaybeRefOrGetter<ProjectPageTable | undefined>
) {
  const editingPage = ref<boolean>(false);

  // Helper setter function, v-model cannot directly edit from inside a slot.
  function changeEditingPage(newValue: boolean) {
    editingPage.value = newValue;
  }

  async function savePage(content: string) {
    const pageVal = toValue(page);
    if (!pageVal) return;
    await useInternalApi(`pages/save/${toValue(project)?.id}/${pageVal?.id}`, "post", {
      content,
    }).catch((err) => handleRequestError(err, "page.new.error.save"));
    if (pageVal && "contents" in pageVal) {
      pageVal.contents = content;
    }
    editingPage.value = false;
  }

  async function deletePage() {
    const pageVal = toValue(page);
    if (!pageVal) return;
    await useInternalApi(`pages/delete/${toValue(project)?.id}/${pageVal.id}`, "post").catch((err) => handleRequestError(err, "page.new.error.save"));
    await router.replace(`/${toValue(route).params.user}/${toValue(route).params.project}`);
  }

  return { editingPage, changeEditingPage, savePage, deletePage };
}
