import { useAuthStore } from "~/store/auth";
import { useCookies } from "~/composables/useCookies";
import { useContext } from "vite-ssr";
import { useInternalApi } from "~/composables/useApi";
import { useAxios } from "~/composables/useAxios";
import { useApiToken } from "~/composables/useApiToken";
// TODO wtf
// eslint-disable-next-line import/no-unresolved
import { User } from "hangar-api";

class Auth {
  loginUrl(redirectUrl: string): string {
    return `/login?returnUrl=${import.meta.env.HANGAR_PUBLIC_HOST}${redirectUrl}`;
  }

  async processLogin(token: string): Promise<void> {
    useAuthStore().$patch({ authenticated: true });
    return await this.updateUser(token);
  }

  async invalidate(shouldRedirect = true) {
    useAuthStore().$patch({
      user: null,
      token: null,
      authenticated: false,
    });
    await useAxios.get("/invalidate").catch(() => console.log("invalidate failed"));
    useCookies().remove("HangarAuth_REFRESH", {
      path: "/",
    });
    if (shouldRedirect) {
      useContext().redirect("/logged-out");
    }
  }

  async updateUser(token: string): Promise<void> {
    const user = await useInternalApi<User>("users/@me", true, "get", {}, {}, token).catch(async (err) => {
      console.log(err);
      console.log("LOGGING OUT ON updateUser");
      return await this.invalidate(!import.meta.env.SSR);
    });
    if (user) {
      useAuthStore().$patch({ user });
    }
  }

  async refreshUser() {
    const token = await useApiToken(true);
    if (!token) {
      return this.invalidate(!import.meta.env.SSR);
    }
    return useAuthStore().authenticated ? await this.updateUser(token) : await this.processLogin(token);
  }
}

export const useAuth = new Auth();
