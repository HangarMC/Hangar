import { useAuthStore } from "~/store/auth";
import { useCookies } from "~/composables/useCookies";
import { useContext } from "vite-ssr";
import { useInternalApi } from "~/composables/useApi";
import { useAxios } from "~/composables/useAxios";
import { authLog } from "~/composables/useLog";
import { HangarUser } from "hangar-internal";
import { useRequest } from "~/composables/useResReq";

class Auth {
  loginUrl(redirectUrl: string): string {
    if (redirectUrl.endsWith("?loggedOut")) {
      redirectUrl = redirectUrl.replace("?loggedOut", "");
    }
    return `/login?returnUrl=${import.meta.env.HANGAR_PUBLIC_HOST}${redirectUrl}`;
  }

  async logout() {
    location.replace(`/logout?returnUrl=${import.meta.env.HANGAR_PUBLIC_HOST}?loggedOut`);
  }

  // TODO do we need to scope this to the user?
  refreshPromise: Promise<boolean> | null = null;

  async refreshToken() {
    authLog("refresh token");
    if (this.refreshPromise) {
      authLog("locked, lets wait");
      const result = await this.refreshPromise;
      authLog("lock over", result);
      return result;
    }

    // eslint-disable-next-line no-async-promise-executor
    this.refreshPromise = new Promise<boolean>(async (resolve) => {
      try {
        authLog("do request");
        await useAxios.get("/refresh");
        authLog("done");
        resolve(true);
        this.refreshPromise = null;
      } catch (e) {
        authLog("Refresh failed", e);
        resolve(false);
      }
    });
    return this.refreshPromise;
  }

  async invalidate(shouldRedirect = true) {
    useAuthStore(this.usePiniaIfPresent()).$patch({
      user: null,
      token: null,
      authenticated: false,
    });
    await useAxios.get("/invalidate").catch(() => console.log("invalidate failed"));
    if (!import.meta.env.SSR) {
      useCookies().remove("HangarAuth_REFRESH", { path: "/" });
      useCookies().remove("HangarAuth", { path: "/" });
      authLog("Invalidated auth cookies");
    }
    if (shouldRedirect) {
      useContext().redirect("/logged-out");
    }
  }

  async updateUser(): Promise<void> {
    const user = await useInternalApi<HangarUser>("users/@me").catch(async (err) => {
      const { trace, ...data } = err.response.data;
      console.log(data);
      console.log("LOGGING OUT ON updateUser");
      return this.invalidate(!import.meta.env.SSR);
    });
    if (user) {
      authLog("patching " + user.name);
      const authStore = useAuthStore(this.usePiniaIfPresent());
      authStore.setUser(user);
      authStore.$patch({ authenticated: true });
      authLog("user is now " + authStore.user?.name);
    }
  }

  usePiniaIfPresent() {
    return import.meta.env.SSR ? useRequest()?.pinia : null;
  }
}

export const useAuth = new Auth();
