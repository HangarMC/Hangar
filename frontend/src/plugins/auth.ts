import { RouteLocationNormalized, RouteLocationRaw } from "vue-router";
import { PermissionCheck, UserPermissions } from "hangar-api";
import { useI18n } from "vue-i18n";
import { NuxtApp } from "nuxt/app";
import { useAuth } from "~/composables/useAuth";
import { routePermLog } from "~/lib/composables/useLog";
import { useAuthStore } from "~/store/auth";
import { useApi } from "~/composables/useApi";
import { useErrorRedirect } from "~/lib/composables/useErrorRedirect";
import { hasPerms, toNamedPermission } from "~/composables/usePerm";
import { NamedPermission, PermissionType } from "~/types/enums";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useSettingsStore } from "~/store/useSettingsStore";
import { defineNuxtPlugin, useRequestEvent, useRouter } from "#imports";

export default defineNuxtPlugin(async (nuxtApp: NuxtApp) => {
  useRouter().beforeEach(async (to, from, next) => {
    if (to.fullPath.startsWith("/@vite")) {
      // really don't need to do stuff for such meta routes
      return;
    }

    await loadPerms(to);

    const result = await handleRoutePerms(to);
    if (result) {
      next(result);
    } else {
      next();
    }
  });
  await useAuth.updateUser();
  if (!process.server) return;
  const event = useRequestEvent();
  const request = event.node.res;
  const response = event.node.res;
  if (request?.url?.includes("/@vite")) {
    // really don't need to do stuff for such meta routes
    return;
  }
  await useSettingsStore().loadSettingsServer(request, response);
});

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

type handlersType = {
  [key: string]: (
    authStore: ReturnType<typeof useAuthStore>,
    to: RouteLocationNormalized
  ) => Promise<RouteLocationRaw | undefined> | RouteLocationRaw | undefined;
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
  const check = await useApi<PermissionCheck>("permissions/hasAll", true, "get", {
    permissions: toNamedPermission(to.meta.globalPermsRequired as string[]),
  }).catch((e) => {
    try {
      routePermLog("error!", e);
      handleRequestError(e, useI18n());
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
