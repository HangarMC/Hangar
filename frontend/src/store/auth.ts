import type { HangarUser } from "hangar-internal";
import { defineStore } from "pinia";
import { ref } from "vue";
import { authLog } from "~/lib/composables/useLog";

export const useAuthStore = defineStore("auth", () => {
  const authenticated = ref<boolean>(false);
  const user = ref<HangarUser | null>(null);
  const routePermissions = ref<string | null>(null);
  const invalidated = ref<boolean>(false);

  authLog("create authStore");

  function setUser(newUser: HangarUser) {
    user.value = newUser;
  }

  function setRoutePerms(routePerms: string | null) {
    routePermissions.value = routePerms;
  }

  return { authenticated, user, routePermissions, invalidated, setUser, setRoutePerms };
});
