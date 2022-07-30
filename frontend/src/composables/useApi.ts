import type { AxiosError, AxiosRequestConfig } from "axios";
import type { HangarApiException } from "hangar-api";
import qs from "qs";
import Cookies from "universal-cookie";
import { useAxios } from "~/composables/useAxios";
import { useCookies } from "~/composables/useCookies";
import { Ref } from "vue";
import { authLog, fetchLog } from "~/lib/composables/useLog";
import { isEmpty } from "lodash-es";
import { useAuth } from "~/composables/useAuth";
import { useRequest, useResponse } from "~/composables/useResReq";
import { Context } from "vite-ssr/vue";

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
            const parsedCookies = new Cookies(statString);
            const statCookie = parsedCookies.get("hangar_stats");
            // keep cookie settings in sync with StatService#setCookie
            cookies.set("hangar_stats", statCookie, { path: "/", sameSite: "strict", maxAge: 60 * 60 * 24 * 356.24 * 1000 });
            authLog("got stats cookie from backend", statCookie);
          }
        }
        resolve(data);
      })
      .catch(async (error: AxiosError) => {
        const { trace, ...err } = error.response?.data as { trace: any };
        authLog("failed", err);
        // do we have an expired token?
        if (error.response?.status === 403) {
          if (retry) {
            // we failed on a retry, let's invalidate
            authLog("failed retry -> invalidate");
            await useAuth.invalidate();
            return reject(error);
          }
          // do we have a refresh token we could use?
          const result = await useAuth.refreshToken();
          if (result) {
            // retry
            authLog("Retrying request...");
            if (typeof result === "string") {
              headers = { ...headers, Authorization: `HangarAuth ${result}` };
              authLog("using new token");
            }
            try {
              const response = await request<T>(url, method, data, headers, true);
              return resolve(response);
            } catch (e) {
              return reject(e);
            }
          } else {
            authLog("Not retrying since refresh failed");
            await useAuth.invalidate();
            return reject(error);
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

export async function useInternalApi<T = void>(
  url: string,
  authed = true,
  method: AxiosRequestConfig["method"] = "get",
  data: object = {},
  headers: Record<string, string> = {}
): Promise<T> {
  fetchLog("useInternalApi", url);
  return processAuthStuff(headers, authed, (headers) => request(`internal/${url}`, method, data, headers));
}

export async function processAuthStuff<T>(headers: Record<string, string>, authRequired: boolean, handler: (headers: Record<string, string>) => Promise<T>) {
  if (import.meta.env.SSR) {
    // forward cookies I guess?
    let token = useCookies().get("HangarAuth");
    let refreshToken = useCookies().get("HangarAuth_REFRESH");
    if (!token) {
      const header = useResponse()?.getHeader("set-cookie") as string[];
      if (header && header.join) {
        const cookies = new Cookies(header.join("; "));
        token = cookies.get("HangarAuth");
        refreshToken = cookies.get("HangarAuth_REFRESH");
        authLog("found token in set-cookie header");
      }
    }
    if (token || refreshToken) {
      authLog("forward token from cookie");

      // make sure our token is still valid, else refresh
      if (!useAuth.validateToken(token)) {
        authLog("token no longer valid, lets refresh");
        const result = await useAuth.refreshToken();
        if (result) {
          authLog("refreshed");
          token = result;
        } else {
          authLog("could not refresh, invalidate");
          await useAuth.invalidate();
          throw {
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
          };
        }
      }

      headers = { ...headers, Authorization: `HangarAuth ${token}`, ...forwardHeader() };
    } else {
      authLog("can't forward token, no cookie");
      if (authRequired) {
        throw {
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
        };
      }
    }
  } else {
    // validate and refresh
    const token = useCookies().get("HangarAuth");
    if (!useAuth.validateToken(token)) {
      authLog("token no longer valid, lets refresh");
      const result = await useAuth.refreshToken();
      if (result) {
        authLog("refreshed");
      } else {
        authLog("could not refresh, invalidate");
        await useAuth.invalidate();
        if (authRequired) {
          throw {
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
          };
        }
      }
    }
  }
  return handler(headers);
}

function forwardHeader(): Record<string, string> {
  const req: Context["request"] = useRequest();
  const result: Record<string, string> = {};
  if (!req) return result;

  const forward = (header: string) => {
    if (req.headers[header]) {
      result[header] = req.headers[header];
    }
  };

  forward("cf-connecting-ip");
  forward("cf-ipcountry");
  forward("x-forwarded-host");
  forward("x-real-ip");

  return result;
}

export async function fetchIfNeeded<T>(func: () => Promise<T>, ref: Ref<T>) {
  if (!isEmpty(ref.value)) {
    return ref;
  }
  ref.value = await func();
  return ref;
}
