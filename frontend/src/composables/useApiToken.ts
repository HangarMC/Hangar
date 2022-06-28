import type { JwtPayload } from "jwt-decode";
import jwtDecode from "jwt-decode";
import { useAuthStore } from "~/store/auth";
import { useAxios } from "~/composables/useAxios";
import { useCookies } from "~/composables/useCookies";
import { authLog } from "~/lib/composables/useLog";

async function refreshToken(): Promise<string | null> {
  const cookie = useCookies().get("HangarAuth_REFRESH");
  authLog("Refreshing token...", cookie);
  const config = import.meta.env.SSR && cookie ? { headers: { Cookie: "HangarAuth_REFRESH=" + useCookies().get("HangarAuth_REFRESH") } } : {};
  const value = await useAxios.get<{ token: string; refreshToken: string; cookieName: string; expiresIn: number }>("/refresh", config).catch((e) => {
    return null;
  });
  if (value) {
    authLog("New refresh token", value.data.refreshToken);
    useAuthStore().$patch({ token: value.data.token });
    useCookies().set(value.data.cookieName, value.data.refreshToken, {
      path: "/",
      expires: new Date(Date.now() + value.data.expiresIn * 1000),
      sameSite: "strict",
      secure: import.meta.env.PROD,
    });
    return value.data.token;
  } else {
    return null;
  }
}

function validateToken(token: string): boolean {
  if (!token) {
    return false;
  }
  const decodedToken = jwtDecode<JwtPayload>(token);
  if (!decodedToken.exp) {
    return false;
  }
  return decodedToken.exp * 1000 > Date.now() - 10 * 1000; // check against 10 seconds earlier to mitigate tokens expiring mid-request
}

export async function useApiToken(forceFetch = true): Promise<string | null> {
  const store = useAuthStore();
  authLog("stored token ", store.token, "valid?", validateToken(store.token || ""));
  if (store.token) {
    return validateToken(store.token) ? store.token : refreshToken();
  } else if (forceFetch) {
    return refreshToken();
  } else {
    return null;
  }
}
