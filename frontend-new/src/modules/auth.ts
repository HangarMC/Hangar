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
import { useErrorRedirect } from "~/composables/useErrorRedirect";

export const install: UserModule = async ({ request, response, router, redirect }) => {
  router.beforeEach(async (to, from, next) => {
    if (to.fullPath.startsWith("/@vite")) {
      // really don't need to do stuff for such meta routes
      return;
    }

    await handleLogin(request, response);
    await loadPerms(to);

    const result = handleRoutePerms(to);
    if (result) {
      next(result);
    } else {
      next();
    }
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
  const authStore = useAuthStore();
  routePermLog("navigate to " + String(to.name) + ", meta:", to.meta);
  if (to.meta.requireLoggedIn === true) {
    routePermLog("route requireLoggedIn");
    const result = checkLogin(authStore, to, 401, "You must be logged in to perform this action");
    if (result) return result;
  }

  if (to.meta.requireNotLoggedIn === true) {
    routePermLog("route requireNotLoggedIn");
    if (authStore.authenticated) {
      return {
        path: "/",
      };
    }
  }

  if (to.meta.requireCurrentUser === true) {
    routePermLog("route requireCurrentUser");
    if (!to.params.user) {
      throw new TypeError("Must have 'user' as a route param to use CurrentUser");
    }
    const result = checkLogin(authStore, to, 404);
    if (result) return result;
    // TODO implement this
    // if (!$perms.canEditAllUserSettings) {
    //   if (to.params.user !== authStore.user?.name) {
    //     return useErrorRedirect(to, 403);
    //   }
    // }
  }

  if (to.meta.requireGlobalPerm) {
    routePermLog("route requireGlobalPerm", to.meta.requireGlobalPerm);
    const result = checkLogin(authStore, to, 403);
    if (result) return result;
    // TODO implement this
  }

  if (to.meta.requireProjectPerm) {
    routePermLog("route requireProjectPerm", to.meta.requireProjectPerm);
    if (!to.params.user || !to.params.project) {
      throw new Error("Can't use this decorator on a route without `author` and `slug` path params");
    }
    const result = checkLogin(authStore, to, 404);
    if (result) return result;
    if (!authStore.routePermissions) {
      return useErrorRedirect(to, 404);
    }
    // TODO implement this
    // if (!$util.hasPerms(...(to.meta.requireProjectPerm as string[]))) {
    //   return useErrorRedirect(to, 404);
    // }
  }
  return;
}

function checkLogin(authStore: ReturnType<typeof useAuthStore>, to: RouteLocationNormalized, status: number, msg?: string) {
  if (!authStore.authenticated) {
    routePermLog("not logged in!");
    return useErrorRedirect(to, status, msg);
  }
  return;
}
