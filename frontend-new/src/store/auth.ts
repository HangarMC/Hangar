import type { HangarUser } from "hangar-internal";
import { defineStore } from "pinia";
import { ref } from "vue";
import { authLog } from "~/composables/useLog";

export const useAuthStore = defineStore("auth", () => {
  const authenticated = ref<boolean>(false);
  const user = ref<HangarUser | null>(null);
  const token = ref<string | null>(null);
  const routePermissions = ref<string | null>(null);

  authLog("create authStore");

  function setUser(newUser: HangarUser) {
    user.value = newUser;
  }

  return { authenticated, user, token, routePermissions, setUser };
});
