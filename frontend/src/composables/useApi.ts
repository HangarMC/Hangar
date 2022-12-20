import type { AxiosError, AxiosRequestConfig } from "axios";
import qs from "qs";
import Cookies from "universal-cookie";
import { isEmpty } from "lodash-es";
import type { Ref } from "vue";
import { useCookies } from "~/composables/useCookies";
import { authLog, fetchLog } from "~/lib/composables/useLog";
import { useAxios } from "~/composables/useAxios";

function request<T>(url: string, method: AxiosRequestConfig["method"], data: object): Promise<T> {
  const cookies = useCookies();
  return new Promise<T>((resolve, reject) => {
    return useAxios()
      .request<T>({
        method,
        url: `/api/${url}`,
        data: method?.toLowerCase() !== "get" ? data : {},
        params: method?.toLowerCase() === "get" ? data : {},
        paramsSerializer: (params) => {
          return qs.stringify(params, {
            arrayFormat: "repeat",
          });
        },
      })
      .then(({ data, headers }) => {
        // check for stats cookie
        if (headers["set-cookie"]) {
          const statString = headers["set-cookie"].find((c: string) => c.startsWith("hangar_stats"));
          if (statString) {
            const parsedCookies = new Cookies(statString);
            const statCookie = parsedCookies.get("hangar_stats");
            // keep cookie settings in sync with StatService#setCookie
            cookies.set("hangar_stats", statCookie, { path: "/", sameSite: "strict", maxAge: 60 * 60 * 24 * 356.24 * 1000 });
            authLog("got stats cookie from backend", statCookie);
          }
        }
        resolve(data);
      })
      .catch((error: AxiosError) => {
        const { trace, ...err } = (error.response?.data as { trace: any }) || {};
        authLog("failed", err, error);
        reject(error);
      });
  });
}

export function useApi<T>(url: string, authed = true, method: AxiosRequestConfig["method"] = "get", data: object = {}): Promise<T> {
  fetchLog("useApi", url);
  return request(`v1/${url}`, method, data);
}

// TODO do something with authed

export function useInternalApi<T = void>(url: string, authed = true, method: AxiosRequestConfig["method"] = "get", data: object = {}): Promise<T> {
  fetchLog("useInternalApi", url);
  return request(`internal/${url}`, method, data);
}

export async function fetchIfNeeded<T>(func: () => Promise<T>, ref: Ref<T>) {
  if (!isEmpty(ref.value)) {
    return ref;
  }
  ref.value = await func();
  return ref;
}
