import type { HangarUser } from "#shared/types/backend";

export const useAuthStore = defineStore("auth", () => {
  const token = ref<string | undefined>(undefined);
  const authenticated = ref<boolean>(false);
  const user = ref<HangarUser | undefined>(undefined);
  const aal = ref(-1);
  const routePermissions = ref<string | undefined>(undefined);
  const routePermissionsUser = ref<string | undefined>(undefined);
  const routePermissionsProject = ref<string | undefined>(undefined);
  const invalidated = ref<boolean>(false);

  authLog("create authStore");

  function setRoutePerms(routePerms?: string, routePermsUser?: string, routePermsProject?: string) {
    routePermissions.value = routePerms;
    routePermissionsUser.value = routePermsUser;
    routePermissionsProject.value = routePermsProject;
  }

  return { token, authenticated, user, routePermissions, invalidated, setRoutePerms, routePermissionsUser, routePermissionsProject, aal };
});
