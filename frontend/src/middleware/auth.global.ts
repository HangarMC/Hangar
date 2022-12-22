import { RouteLocationNamedRaw, RouteLocationNormalized } from "vue-router";
import { Composer, useI18n } from "vue-i18n";
import { PermissionCheck, UserPermissions } from "hangar-api";
import { defineNuxtRouteMiddleware, handleRequestError, hasPerms, navigateTo, toNamedPermission, useApi, useAuth } from "#imports";
import { useAuthStore } from "~/store/auth";
import { routePermLog } from "~/lib/composables/useLog";
import { NamedPermission, PermissionType } from "~/types/enums";
import { useErrorRedirect } from "~/lib/composables/useErrorRedirect";
import { I18n } from "~/lib/i18n";

export default defineNuxtRouteMiddleware(async (to: RouteLocationNormalized, from: RouteLocationNormalized) => {
  if (to.fullPath.startsWith("/@vite")) {
    // really don't need to do stuff for such meta routes
    console.log("hit vite path???????????????????????", to.fullPath);
    return;
  }

  await useAuth.updateUser();
  await loadRoutePerms(to);
  const result = await handleRoutePerms(to);
  if (result) {
    return navigateTo(result, { redirectCode: result.params?.status });
  }
});

async function loadRoutePerms(to: RouteLocationNormalized) {
  const authStore = useAuthStore();
  if (to.params.user && to.params.project) {
    if (authStore.authenticated) {
      const perms = await useApi<UserPermissions>("permissions", "get", {
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
      const perms = await useApi<UserPermissions>("permissions", "get", {
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

type handlersType = {
  [key: string]: (
    authStore: ReturnType<typeof useAuthStore>,
    to: RouteLocationNormalized
  ) => Promise<RouteLocationNamedRaw | undefined> | RouteLocationNamedRaw | undefined;
};
const handlers: handlersType = {
  currentUserRequired,
  globalPermsRequired,
  projectPermsRequired,
};

function currentUserRequired(authStore: ReturnType<typeof useAuthStore>, to: RouteLocationNormalized) {
  routePermLog("route currentUserRequired");
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

async function globalPermsRequired(authStore: ReturnType<typeof useAuthStore>, to: RouteLocationNormalized) {
  routePermLog("route globalPermsRequired", to.meta.globalPermsRequired);
  const result = checkLogin(authStore, to, 403);
  if (result) return result;
  const check = await useApi<PermissionCheck>("permissions/hasAll", "get", {
    permissions: toNamedPermission(to.meta.globalPermsRequired as string[]),
  }).catch((e) => {
    try {
      routePermLog("error!", e);
      handleRequestError(e);
    } catch (e2) {
      routePermLog("error while checking perm", e);
      routePermLog("encountered additional error while error handling", e2);
    }
  });
  routePermLog("result", check);
  if (check && (check.type !== PermissionType.GLOBAL || !check.result)) {
    routePermLog("404?");
    return useErrorRedirect(to, 404, "Not found");
  }
}

function projectPermsRequired(authStore: ReturnType<typeof useAuthStore>, to: RouteLocationNormalized) {
  routePermLog("route projectPermsRequired", to.meta.projectPermsRequired);
  if (!to.params.user || !to.params.project) {
    throw new Error("Can't use this decorator on a route without `author` and `slug` path params");
  }
  const result = checkLogin(authStore, to, 404);
  if (result) return result;
  if (!authStore.routePermissions) {
    return useErrorRedirect(to, 404);
  }
  routePermLog("check has perms", to.meta.projectPermsRequired);
  if (!hasPerms(...toNamedPermission(to.meta.projectPermsRequired as string[]))) {
    return useErrorRedirect(to, 404);
  }
}

function checkLogin(authStore: ReturnType<typeof useAuthStore>, to: RouteLocationNormalized, status: number, msg?: string) {
  if (!authStore.authenticated) {
    routePermLog("not logged in!");
    return useErrorRedirect(to, status, msg);
  }
}
