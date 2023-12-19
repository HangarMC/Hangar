import type { RouteLocationNamedRaw, RouteLocationNormalized } from "vue-router";
import { createError } from "#imports";

export function useErrorRedirect(
  currentRoute: RouteLocationNormalized,
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
