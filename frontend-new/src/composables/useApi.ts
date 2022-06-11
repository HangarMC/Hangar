import type { AxiosError, AxiosRequestConfig } from "axios";
import type { HangarApiException } from "hangar-api";
import qs from "qs";
import Cookies from "universal-cookie";
import { useAxios } from "~/composables/useAxios";
import { useCookies } from "~/composables/useCookies";
import { Ref } from "vue";
import { authLog, fetchLog } from "~/composables/useLog";
import { isEmpty } from "lodash-es";
import { useAuth } from "~/composables/useAuth";

interface StatCookie {
  // eslint-disable-next-line camelcase
  hangar_stats: string;
  Path: string;
  "Max-Age": string;
  Expires: string;
  SameSite: true | false | "lax" | "strict" | "none" | undefined;
  Secure?: boolean;
}

function request<T>(url: string, method: AxiosRequestConfig["method"], data: object, headers: Record<string, string> = {}, retry = false): Promise<T> {
  const cookies = useCookies();
  return new Promise<T>((resolve, reject) => {
    return useAxios
      .request<T>({
        method,
        url: `/api/${url}`,
        headers,
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
            const statCookie: StatCookie = new Cookies("statString") as unknown as StatCookie;

            cookies.set("hangar_stats", statCookie.hangar_stats, {
              path: statCookie.Path,
              expires: new Date(statCookie.Expires),
              sameSite: "strict",
              secure: statCookie.Secure,
            });
          }
        }
        resolve(data);
      })
      .catch(async (error: AxiosError) => {
        authLog("failed", error);
        // do we have an expired token?
        if (error.response?.status === 403) {
          if (retry) {
            // we failed on a retry, let's invalidate
            authLog("failed retry -> invalidate");
            return useAuth.invalidate(!import.meta.env.SSR);
          }
          // do we have a refresh token we could use?
          const result = await useAuth.refreshToken();
          if (result) {
            // retry
            authLog("Retrying request...");
            try {
              const response = await request<T>(url, method, data, headers, true);
              resolve(response);
            } catch (e) {
              reject(e);
            }
          } else {
            authLog("Not retrying since refresh failed");
            return useAuth.invalidate(!import.meta.env.SSR);
          }
        }
        reject(error);
      });
  });
}

export async function useApi<T>(
  url: string,
  authed = true,
  method: AxiosRequestConfig["method"] = "get",
  data: object = {},
  headers: Record<string, string> = {}
): Promise<T> {
  fetchLog("useApi", url);
  return processAuthStuff(headers, authed, (headers) => request(`v1/${url}`, method, data, headers));
}

export async function useInternalApi<T>(
  url: string,
  authed = true,
  method: AxiosRequestConfig["method"] = "get",
  data: object = {},
  headers: Record<string, string> = {}
): Promise<T> {
  fetchLog("useInternalApi", url);
  return processAuthStuff(headers, authed, (headers) => request(`internal/${url}`, method, data, headers));
}

function processAuthStuff<T>(headers: Record<string, string>, authRequired: boolean, handler: (headers: Record<string, string>) => Promise<T>) {
  if (import.meta.env.SSR) {
    // forward cookies I guess?
    const token = useCookies().get("HangarAuth");
    if (token) {
      authLog("forward token from cookie");
      headers = { ...headers, Authorization: `HangarAuth ${token}` };
    } else {
      authLog("can't forward token, no cookie");
      if (authRequired) {
        return Promise.reject({
          isAxiosError: true,
          response: {
            data: {
              isHangarApiException: true,
              httpError: {
                statusCode: 401,
              },
              message: "You must be logged in",
            } as HangarApiException,
          },
        });
      }
    }
  } else {
    // don't need to do anything, cookies are handled by the browser
  }
  return handler(headers);
}

export async function fetchIfNeeded<T>(func: () => Promise<T>, ref: Ref<T>) {
  if (!isEmpty(ref.value)) {
    return ref;
  }
  ref.value = await func();
  return ref;
}
