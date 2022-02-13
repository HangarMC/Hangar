import type { HangarUser } from "hangar-internal";

export const useAuthStore = defineStore("auth", {
  state: () => {
    return {
      authenticated: false,
      user: null as HangarUser | null,
      token: null as string | null,
      routePermissions: null as string | null,
    };
  },
});
