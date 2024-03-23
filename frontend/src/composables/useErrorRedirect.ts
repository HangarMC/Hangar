import type { RouteLocationNormalizedTyped } from "unplugin-vue-router";
import type { RouteLocationNamedRaw } from "vue-router";
import type { HangarNuxtError } from "~/types/components/error";

export function useErrorRedirect(currentRoute: RouteLocationNormalizedTyped<any>, status: number, msg?: string, data?: HangarNuxtError): RouteLocationNamedRaw {
  throw createError({
    statusCode: status,
    message: msg,
    data,
  });
}

export function useDummyError() {
  throw createError<HangarNuxtError>({ message: "dummy", data: { logErrorMessage: false, dummyError: true } });
}
