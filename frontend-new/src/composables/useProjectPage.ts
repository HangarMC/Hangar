import { ref, watch } from "vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { RouteLocationNormalizedLoaded, Router, useRoute, useRouter } from "vue-router";
import { Context } from "vite-ssr/vue";
import { Composer, VueMessageType } from "vue-i18n";
import { HangarProject } from "hangar-internal";
import { usePage } from "~/composables/useApiHelper";
import { useErrorRedirect } from "~/composables/useErrorRedirect";

export async function useProjectPage(
  route: RouteLocationNormalizedLoaded,
  router: Router,
  ctx: Context,
  i18n: Composer<unknown, unknown, unknown, VueMessageType>,
  project: HangarProject
) {
  const page = await usePage(route.params.user as string, route.params.project as string, route.params.all as string).catch((e) =>
    handleRequestError(e, ctx, i18n)
  );
  if (!page) {
    await useRouter().push(useErrorRedirect(useRoute(), 404, "Not found"));
  }

  const editingPage = ref<boolean>(false);
  const open = ref<string[]>([]);

  watch(
    route,
    () => {
      const slugs = route.fullPath.split("/").slice(4);
      if (slugs.length) {
        for (let i = 0; i < slugs.length; i++) {
          const slug = slugs.slice(0, i + 1).join("/");
          if (!open.value.includes(slug)) {
            open.value.push(slug);
          }
        }
      } else if (project.pages.length === 1) {
        open.value.push(project.pages[0].slug);
      }
    },
    { immediate: true }
  );

  async function savePage(content: string) {
    if (!page) return;
    await useInternalApi(`pages/save/${project.id}/${page.value?.id}`, true, "post", {
      content,
    }).catch((e) => handleRequestError(e, ctx, i18n, "page.new.error.save"));
    // todo page saving
    //page.value?.contents = content;
    editingPage.value = false;
  }

  async function deletePage() {
    if (!page) return;
    await useInternalApi(`pages/delete/${project.id}/${page.value?.id}`, true, "post").catch((e) => handleRequestError(e, ctx, i18n, "page.new.error.save"));
    // todo page deleting
    //this.$refs.editor.loading.delete = false;
    await router.replace(`/${route.params.user}/${route.params.project}`);
  }
  return { editingPage, open, page, savePage, deletePage };
}
