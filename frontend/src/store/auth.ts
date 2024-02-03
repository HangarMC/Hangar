import type { HangarUser } from "~/types/backend";

export const useAuthStore = defineStore("auth", () => {
  const token = ref<string | null>(null);
  const authenticated = ref<boolean>(false);
  const user = ref<HangarUser | null>(null);
  const aal = ref(-1);
  const routePermissions = ref<string | null>(null);
  const routePermissionsUser = ref<string | null>(null);
  const routePermissionsProject = ref<string | null>(null);
  const invalidated = ref<boolean>(false);

  authLog("create authStore");

  function setRoutePerms(routePerms: string | null, routePermsUser: string | null = null, routePermsProject: string | null = null) {
    routePermissions.value = routePerms;
    routePermissionsUser.value = routePermsUser;
    routePermissionsProject.value = routePermsProject;
  }

  return { token, authenticated, user, routePermissions, invalidated, setRoutePerms, routePermissionsUser, routePermissionsProject, aal };
});
