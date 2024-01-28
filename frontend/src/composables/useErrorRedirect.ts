import type { RouteLocationNormalizedTyped } from "unplugin-vue-router";
import type { RouteLocationNamedRaw } from "vue-router";
import { createError } from "#imports";

export function useErrorRedirect(
  currentRoute: RouteLocationNormalizedTyped<any>,
  status: number,
  msg?: string,
  data?: { dummyError?: boolean; logErrorMessage?: boolean }
): RouteLocationNamedRaw {
  throw createError({
    status,
    message: msg,
    data,
  });
}

export function useDummyError() {
  throw createError({ message: "dummy", data: { logErrorMessage: false, dummyError: true } });
}
