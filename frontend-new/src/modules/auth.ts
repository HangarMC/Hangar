import type { UserModule } from "~/types";
import { useAuth } from "~/composables/useAuth";
import { useCookies } from "~/composables/useCookies";
import { set, unset } from "~/composables/useResReq";
import { authLog, routePermLog } from "~/composables/useLog";
import { useAuthStore } from "~/store/auth";
import { RouteLocationNormalized } from "vue-router";
import { Context } from "vite-ssr/vue";
import { useApi } from "~/composables/useApi";
import { UserPermissions } from "hangar-api";

export const install: UserModule = async ({ request, response, router }) => {
  router.beforeEach(async (to) => {
    if (to.fullPath.startsWith("/@vite")) {
      // really don't need to do stuff for such meta routes
      return;
    }

    await handleLogin(request, response);
    await loadPerms(to);
    handleRoutePerms(to);
  });
};

async function handleLogin(request: Context["request"], response: Context["response"]) {
  set(request, response);
  if (useCookies().get("HangarAuth_REFRESH")) {
    authLog("Got refresh cookie, calling refresh...");
    await useAuth.refreshUser();
  } else if (useAuthStore().authenticated) {
    authLog("Logged in on the backend, calling refresh...");
    await useAuth.refreshUser();
  }
  unset();
}

async function loadPerms(to: RouteLocationNormalized) {
  const authStore = useAuthStore();
  if (to.params.user && to.params.project) {
    if (authStore.authenticated) {
      const perms = await useApi<UserPermissions>("permissions", true, "get", {
        author: to.params.user,
        slug: to.params.project,
      }).catch(() => authStore.setRoutePerms(null));
      if (perms) {
        authStore.setRoutePerms(perms.permissionBinString);
      }
      return;
    }
  } else if (to.params.user) {
    if (authStore.authenticated) {
      const perms = await useApi<UserPermissions>("permissions", true, "get", {
        organization: to.params.user,
      }).catch(() => authStore.setRoutePerms(null));
      if (perms) {
        authStore.setRoutePerms(perms.permissionBinString);
      }
      return;
    }
  } else if (authStore.authenticated) {
    // fall back to global perms
    authStore.setRoutePerms(authStore.user?.headerData?.globalPermission || null);
    return;
  }
  authStore.setRoutePerms(null);
}

function handleRoutePerms(to: RouteLocationNormalized) {
  // TODO implement route guards
  routePermLog("navigate to " + String(to.name) + ", meta:", to.meta);
  if (to.meta.requireLoggedIn === true) {
    routePermLog("route requireLoggedIn");
  }
  if (to.meta.requireNotLoggedIn === true) {
    routePermLog("route requireNotLoggedIn");
  }
  if (to.meta.requireCurrentUser === true) {
    routePermLog("route requireCurrentUser");
  }
  if (to.meta.requireGlobalPerm) {
    routePermLog("route requireGlobalPerm", to.meta.requireGlobalPerm);
  }
  if (to.meta.requireProjectPerm) {
    routePermLog("route requireProjectPerm", to.meta.requireProjectPerm);
  }
}
