import type { HangarUser } from "hangar-internal";
import type { AxiosError, AxiosInstance, AxiosRequestHeaders } from "axios";
import { jwtDecode, type JwtPayload } from "jwt-decode";
import { useAuthStore } from "~/store/auth";
import { useInternalApi } from "~/composables/useApi";
import { authLog } from "~/composables/useLog";
import { handleRequestError, useCookie, useRequestEvent } from "#imports";
import { useAxios } from "~/composables/useAxios";
import { useNotificationStore } from "~/store/notification";
import { transformAxiosError } from "~/composables/useErrorHandling";

class Auth {
  loginUrl(redirectUrl: string | undefined): string {
    return this.buildUrl(redirectUrl, "/auth/login");
  }

  signupUrl(redirectUrl: string | undefined): string {
    return this.buildUrl(redirectUrl, "/auth/signup");
  }

  private buildUrl(redirectUrl: string | undefined, url: string) {
    if (redirectUrl?.endsWith("?loggedOut")) {
      redirectUrl = redirectUrl.replace("?loggedOut", "");
    }
    if (redirectUrl?.startsWith("/auth") && !redirectUrl.startsWith("/auth/settings")) {
      redirectUrl = undefined;
    }
    return redirectUrl ? `${url}?returnUrl=${redirectUrl}` : url;
  }

  async logout() {
    const result = await useAxios()
      .get(`/api/internal/auth/logout`)
      .catch((e) => handleRequestError(e));
    if (result?.status === 200) {
      location.replace("/?loggedOut");
    } else {
      await useNotificationStore().error("Error while logging out?!");
    }
  }

  validateToken(token: unknown): token is string {
    if (!token || typeof token !== "string") {
      return false;
    }
    const decoded = jwtDecode<JwtPayload>(token);
    if (!decoded.exp) {
      return false;
    }
    return decoded.exp * 1000 > Date.now() - 10 * 1000; // check against 10 seconds earlier to mitigate tokens expiring mid-request
  }

  // TODO do we need to scope this to the user?
  refreshPromise: Promise<false | string> | null = null;

  async refreshToken(): Promise<false | string> {
    // we use a promise as a lock here to make sure only one request is doing a refresh, avoids too many requests
    authLog("refresh token");
    if (this.refreshPromise) {
      authLog("locked, lets wait");
      const result = await this.refreshPromise;
      authLog("lock over", result);
      return result;
    }
    // eslint-disable-next-line no-async-promise-executor
    this.refreshPromise = new Promise<false | string>(async (resolve) => {
      const refreshToken = useCookie("HangarAuth_REFRESH").value;
      if (import.meta.env.SSR && !refreshToken) {
        authLog("no cookie, no point in refreshing");
        resolve(false);
        this.refreshPromise = null;
        return;
      }

      try {
        authLog("do refresh request");
        const headers = {} as AxiosRequestHeaders;
        if (import.meta.env.SSR) {
          headers.cookie = "HangarAuth_REFRESH=" + refreshToken;
          authLog("pass refresh cookie", refreshToken);
        }
        const response = await useAxios().get("/api/internal/auth/refresh", { headers });
        if (response.status === 299) {
          authLog("had no cookie");
          resolve(false);
        } else if (response.status === 200) {
          // forward cookie header to renew refresh cookie
          if (import.meta.env.SSR && response.headers["set-cookie"]) {
            useRequestEvent().node.res?.setHeader("set-cookie", response.headers["set-cookie"]);
          }
          // validate and return token
          const token = response.data;
          if (useAuth.validateToken(token)) {
            authLog("got valid token");
            resolve(response.data);
          } else {
            authLog("refreshed token is not valid?", token);
            resolve(false);
          }
        }
        this.refreshPromise = null;
      } catch (e) {
        this.refreshPromise = null;
        if ((e as AxiosError).response?.data) {
          authLog("Refresh failed", transformAxiosError(e));
        } else {
          authLog("Refresh failed");
        }
        resolve(false);
      }
    });
    return this.refreshPromise;
  }

  async invalidate(axios: AxiosInstance) {
    const store = useAuthStore();
    store.$patch({
      user: null,
      authenticated: false,
      token: null,
    });
    if (!store.invalidated) {
      await axios.get("/api/internal/auth/invalidate").catch((e) => authLog("Invalidate failed", e.message));
    }
    store.invalidated = true;
  }

  async updateUser(): Promise<void> {
    const authStore = useAuthStore();
    const axios = useAxios();
    if (authStore.invalidated) {
      authLog("no point in updating if we just invalidated");
      return;
    }
    if (authStore.user) {
      authLog("no point in updating if we already have a user");
      return;
    }
    const user = await useInternalApi<HangarUser>("users/@me").catch((err) => {
      authLog("no user, with err", transformAxiosError(err));
      return this.invalidate(axios);
    });
    if (user) {
      authLog("patching " + user.name);
      authStore.user = user;
      authStore.authenticated = true;
      authStore.invalidated = false;
      if (user.accessToken) {
        authStore.token = user.accessToken;
      }
      if (user.aal) {
        authStore.aal = user.aal;
      }
      authLog("user is now " + authStore.user?.name);
    } else {
      authLog("no user, no content");
    }
  }
}

export const useAuth = new Auth();
