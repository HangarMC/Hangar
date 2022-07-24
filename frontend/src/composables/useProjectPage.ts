import { ref } from "vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { RouteLocationNormalizedLoaded, Router } from "vue-router";
import { Context } from "vite-ssr/vue";
import { Composer, VueMessageType } from "vue-i18n";
import { HangarProject } from "hangar-internal";
import { usePage } from "~/composables/useApiHelper";
import { useErrorRedirect } from "~/lib/composables/useErrorRedirect";

export async function useProjectPage(
  route: RouteLocationNormalizedLoaded,
  router: Router,
  ctx: Context,
  i18n: Composer<unknown, unknown, unknown, VueMessageType>,
  project: HangarProject
) {
  const sanitizedProjectPageSlug = (route.params.all as string).replace(/\/+$/, "");
  const page = await usePage(route.params.user as string, route.params.project as string, sanitizedProjectPageSlug).catch((e) =>
    handleRequestError(e, ctx, i18n)
  );
  if (!page) {
    await router.push(useErrorRedirect(route, 404, "Not found"));
  }

  const editingPage = ref<boolean>(false);

  // Helper setter function, v-model cannot directly edit from inside a slot.
  function changeEditingPage(newValue: boolean) {
    editingPage.value = newValue;
  }

  async function savePage(content: string) {
    if (!page) return;
    await useInternalApi(`pages/save/${project.id}/${page.value?.id}`, true, "post", {
      content,
    }).catch((e) => handleRequestError(e, ctx, i18n, "page.new.error.save"));
    if (page.value) {
      page.value.contents = content;
    }
    editingPage.value = false;
  }

  async function deletePage() {
    if (!page) return;
    await useInternalApi(`pages/delete/${project.id}/${page.value?.id}`, true, "post").catch((e) => handleRequestError(e, ctx, i18n, "page.new.error.save"));
    await router.replace(`/${route.params.user}/${route.params.project}`);
  }

  return { editingPage, changeEditingPage, page, savePage, deletePage };
}
