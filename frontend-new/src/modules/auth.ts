import type { UserModule } from "~/types";
import { useAuth } from "~/composables/useAuth";
import { useCookies } from "~/composables/useCookies";
import { set, unset } from "~/composables/useResReq";
import { authLog, routePermLog } from "~/composables/useLog";
import { useAuthStore } from "~/store/auth";
import { RouteLocationNormalized, RouteLocationRaw } from "vue-router";
import { Context, useContext } from "vite-ssr/vue";
import { useApi } from "~/composables/useApi";
import { PermissionCheck, UserPermissions } from "hangar-api";
import { useErrorRedirect } from "~/composables/useErrorRedirect";
import { hasPerms, toNamedPermission } from "~/composables/usePerm";
import { NamedPermission, PermissionType } from "~/types/enums";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useI18n } from "vue-i18n";

export const install: UserModule = async ({ request, response, router, redirect }) => {
  router.beforeEach(async (to, from, next) => {
    if (to.fullPath.startsWith("/@vite")) {
      // really don't need to do stuff for such meta routes
      return;
    }

    await handleLogin(request, response);
    await loadPerms(to);

    const result = await handleRoutePerms(to);
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

async function handleRoutePerms(to: RouteLocationNormalized) {
  const authStore = useAuthStore();
  routePermLog("navigate to " + String(to.name) + ", meta:", to.meta);
  for (const key in handlers) {
    if (!to.meta[key]) continue;
    const handler = handlers[key];
    const result = await handler(authStore, to);
    routePermLog("result", result);
    if (result) return result;
  }
}

type handlersType = { [key: string]: (authStore: ReturnType<typeof useAuthStore>, to: RouteLocationNormalized) => Promise<RouteLocationRaw | undefined> };
const handlers: handlersType = {
  requireLoggedIn: requireLoggedIn,
  requireNotLoggedIn: requireNotLoggedIn,
  requireCurrentUser: requireCurrentUser,
  requireGlobalPerm: requireGlobalPerm,
  requireProjectPerm: requireProjectPerm,
};

async function requireLoggedIn(authStore: ReturnType<typeof useAuthStore>, to: RouteLocationNormalized) {
  routePermLog("route requireLoggedIn");
  const result = checkLogin(authStore, to, 401, "You must be logged in to perform this action");
  if (result) return result;
}

async function requireNotLoggedIn(authStore: ReturnType<typeof useAuthStore>, to: RouteLocationNormalized) {
  routePermLog("route requireNotLoggedIn");
  if (authStore.authenticated) {
    return {
      path: "/",
    };
  }
}

async function requireCurrentUser(authStore: ReturnType<typeof useAuthStore>, to: RouteLocationNormalized) {
  routePermLog("route requireCurrentUser");
  if (!to.params.user) {
    throw new TypeError("Must have 'user' as a route param to use CurrentUser");
  }
  const result = checkLogin(authStore, to, 404);
  if (result) return result;
  if (!hasPerms(NamedPermission.EDIT_ALL_USER_SETTINGS)) {
    if (to.params.user !== authStore.user?.name) {
      return useErrorRedirect(to, 403);
    }
  }
}

async function requireGlobalPerm(authStore: ReturnType<typeof useAuthStore>, to: RouteLocationNormalized) {
  routePermLog("route requireGlobalPerm", to.meta.requireGlobalPerm);
  const result = checkLogin(authStore, to, 403);
  if (result) return result;
  const check = await useApi<PermissionCheck>("permissions/hasAll", true, "get", {
    permissions: toNamedPermission(to.meta.requireGlobalPerm as string[]),
  }).catch((e) => handleRequestError(e, useContext(), useI18n()));
  if (check && (check.type !== PermissionType.GLOBAL || !check.result)) {
    return useErrorRedirect(to, 404, "Not found");
  }
}

async function requireProjectPerm(authStore: ReturnType<typeof useAuthStore>, to: RouteLocationNormalized) {
  routePermLog("route requireProjectPerm", to.meta.requireProjectPerm);
  if (!to.params.user || !to.params.project) {
    throw new Error("Can't use this decorator on a route without `author` and `slug` path params");
  }
  const result = checkLogin(authStore, to, 404);
  if (result) return result;
  if (!authStore.routePermissions) {
    return useErrorRedirect(to, 404);
  }
  routePermLog("check has perms", to.meta.requireProjectPerm);
  if (!hasPerms(...toNamedPermission(to.meta.requireProjectPerm as string[]))) {
    return useErrorRedirect(to, 404);
  }
}

function checkLogin(authStore: ReturnType<typeof useAuthStore>, to: RouteLocationNormalized, status: number, msg?: string) {
  if (!authStore.authenticated) {
    routePermLog("not logged in!");
    return useErrorRedirect(to, status, msg);
  }
}
