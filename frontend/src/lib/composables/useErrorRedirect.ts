import { RouteLocationNamedRaw, RouteLocationNormalized } from "vue-router";

export function useErrorRedirect(currentRoute: RouteLocationNormalized, status: number, msg?: string): RouteLocationNamedRaw {
  return {
    name: "error",
    params: {
      // Match the path of your current page and keep the same url...
      pathMatch: currentRoute.path.split("/").slice(1),
      // pass other params
      status,
      msg,
    },
    // ...and the same query and hash.
    query: currentRoute.query,
    hash: currentRoute.hash,
  };
}
