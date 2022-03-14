import { RouteLocationNormalized } from "vue-router";

export function useErrorRedirect(currentRoute: RouteLocationNormalized, status: number, msg?: string) {
  return {
    name: "error",
    params: {
      // Match the path of your current page and keep the same url...
      pathMatch: currentRoute.path.split("/").slice(1),
      // pass other params
      status: status,
      msg: msg,
    },
    // ...and the same query and hash.
    query: currentRoute.query,
    hash: currentRoute.hash,
  };
}
