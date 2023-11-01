import type { RouteLocationNamedRaw, RouteLocationNormalized } from "vue-router";
import { createError } from "#imports";

export function useErrorRedirect(currentRoute: RouteLocationNormalized, status: number, msg?: string): RouteLocationNamedRaw {
  throw createError({
    status,
    message: msg,
  });
}
