import type { RouteLocationTyped, RouteMapGeneric } from "vue-router";
import type { HangarProject } from "#shared/types/backend";

export function useOpenProjectPages(route: RouteLocationTyped<RouteMapGeneric, "user-project-pages-page">, project?: HangarProject) {
  const open = ref<string[]>([]);

  watch(
    route,
    () => {
      const slugs = route.path.split("/").slice(4);
      if (slugs.length > 0) {
        for (let i = 0; i < slugs.length; i++) {
          const slug = slugs.slice(0, i + 1).join("/");
          if (!open.value.includes(slug)) {
            open.value.push(slug);
          }
        }
      } else if (project?.pages?.length === 1) {
        open.value.push(project.pages[0]!.slug);
      }
    },
    { immediate: true }
  );

  return open;
}
