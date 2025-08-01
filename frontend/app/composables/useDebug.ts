import type { NamedPermission } from "#shared/types/backend";

export function useDebug() {
  window.hangarDebug = {};
  window.hangarDebug.debugPerms = () => {
    console.log(useAuthStore().routePermissions);

    const result = {} as Record<NamedPermission, object>;
    for (const perm of useBackendData.permissions.keys()) {
      result[perm] = { hasPerm: hasPerms(perm), perm: useBackendData.permissions.get(perm)?.permission.toString(2) };
    }
    console.table(result);
  };

  window.hangarDebug.backendData = () => useBackendData;

  window.hangarDebug.nuxtApp = () => window?.useNuxtApp?.();

  window.hangarDebug.vueApp = () => window?.useNuxtApp?.().vueApp;

  window.hangarDebug.pinia = () => structuredClone(window?.useNuxtApp?.().$pinia.state.value);
}
