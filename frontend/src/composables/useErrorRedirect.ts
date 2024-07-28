import type { RouteLocationNormalizedTyped } from "unplugin-vue-router";
import type { HangarNuxtError } from "~/types/components/error";

export function useErrorRedirect(currentRoute: RouteLocationNormalizedTyped<any>, status: number, msg?: string, data?: HangarNuxtError) {
  const globalError = useError();
  if (globalError.value) {
    // we are already in an error state, do nothing
    return;
  }
  throw createError({
    statusCode: status,
    message: msg,
    data,
  });
}

export function useDummyError() {
  throw createError<HangarNuxtError>({ message: "dummy", data: { logErrorMessage: false, dummyError: true } });
}
