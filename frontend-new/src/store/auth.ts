import type { HangarUser } from "hangar-internal";
import { defineStore } from "pinia";
import { ref } from "vue";

export const useAuthStore = defineStore("auth", () => {
  const authenticated = ref<boolean>(false);
  const user = ref<HangarUser | null>(null);
  const token = ref<string | null>(null);
  const routePermissions = ref<string | null>(null);

  return { authenticated, user, token, routePermissions };
});
