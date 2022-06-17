import type { HangarUser } from "hangar-internal";
import { defineStore } from "pinia";
import { ref } from "vue";
import { authLog } from "~/composables/useLog";

export const useAuthStore = defineStore("auth", () => {
  const authenticated = ref<boolean>(false);
  const user = ref<HangarUser | null>(null);
  const routePermissions = ref<string | null>(null);

  authLog("create authStore");

  function setUser(newUser: HangarUser) {
    user.value = newUser;
  }

  function setRoutePerms(routePerms: string | null) {
    routePermissions.value = routePerms;
  }

  return { authenticated, user, routePermissions, setUser, setRoutePerms };
});
