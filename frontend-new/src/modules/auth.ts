import type { UserModule } from "~/types";
import { useAuth } from "~/composables/useAuth";
import { useCookies } from "~/composables/useCookies";
import { set, unset } from "~/composables/useResReq";
import { authLog } from "~/composables/useLog";
import { useAuthStore } from "~/store/auth";

export const install: UserModule = async ({ request, response, router }) => {
  router.beforeEach(async (to) => {
    if (to.fullPath.startsWith("/@vite")) {
      // really don't need to do stuff for such meta routes
      return;
    }
    set(request, response);
    if (useCookies().get("HangarAuth_REFRESH")) {
      authLog("Got refresh cookie, calling refresh...");
      await useAuth.refreshUser();
    } else if (useAuthStore().authenticated) {
      authLog("Logged in on the backend, calling refresh...");
      await useAuth.refreshUser();
    }
    // TODO load route permissions here
    unset();
  });
};
