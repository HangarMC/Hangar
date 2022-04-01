import { useAuthStore } from "~/store/auth";
import { useCookies } from "~/composables/useCookies";
import { useContext } from "vite-ssr";
import { useInternalApi } from "~/composables/useApi";
import { useAxios } from "~/composables/useAxios";
import { useApiToken } from "~/composables/useApiToken";
import { authLog } from "~/composables/useLog";
import { HangarUser } from "hangar-internal";
import { useRequest } from "~/composables/useResReq";

class Auth {
  loginUrl(redirectUrl: string): string {
    return `/login?returnUrl=${import.meta.env.HANGAR_PUBLIC_HOST}${redirectUrl}`;
  }

  async processLogin(token: string): Promise<void> {
    useAuthStore(this.usePiniaIfPresent()).$patch({ authenticated: true });
    return await this.updateUser(token);
  }

  async logout() {
    const token = await useApiToken(true);
    location.replace(`/logout?returnUrl=${import.meta.env.HANGAR_PUBLIC_HOST}?loggedOut&t=${token}`);
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
      authLog("Invalidate refresh cookie");
    }
    if (shouldRedirect) {
      useContext().redirect("/logged-out");
    }
  }

  async updateUser(token: string): Promise<void> {
    const user = await useInternalApi<HangarUser>("users/@me", true, "get", {}, {}, token).catch(async (err) => {
      console.log(err);
      console.log("LOGGING OUT ON updateUser");
      return this.invalidate(!import.meta.env.SSR);
    });
    if (user) {
      authLog("patching " + user.name);
      useAuthStore(this.usePiniaIfPresent()).setUser(user);
      authLog("user is now " + useAuthStore(this.usePiniaIfPresent()).user?.name);
    }
  }

  async refreshUser() {
    const token = await useApiToken(true);
    if (!token) {
      authLog("Got no token in refreshUser, invalidate!");
      return this.invalidate(false);
    }
    return useAuthStore(this.usePiniaIfPresent()).authenticated ? this.updateUser(token) : this.processLogin(token);
  }

  usePiniaIfPresent() {
    return useRequest()?.pinia;
  }
}

export const useAuth = new Auth();
