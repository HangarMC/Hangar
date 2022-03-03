import { useCookies } from "@vueuse/integrations/useCookies";
import type { AxiosError, AxiosRequestConfig } from "axios";
import type { HangarApiException } from "hangar-api";
import qs from "qs";
import Cookies from "universal-cookie";
import { useApiToken } from "~/composables/useApiToken";
import { useAxios } from "~/composables/useAxios";

interface StatCookie {
  // eslint-disable-next-line camelcase
  hangar_stats: string;
  Path: string;
  "Max-Age": string;
  Expires: string;
  SameSite: true | false | "lax" | "strict" | "none" | undefined;
  Secure?: boolean;
}

function request<T>(url: string, token: string | null, method: AxiosRequestConfig["method"], data: object, header: Record<string, string> = {}): Promise<T> {
  const headers: Record<string, string> = token ? { ...header, Authorization: `HangarAuth ${token}` } : header;
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

            const cookies = useCookies();
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
      .catch((error: AxiosError) => {
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
  const token = await useApiToken(authed);
  return request(`v1/${url}`, token, method, data, headers);
}

export async function useInternalApi<T>(
  url: string,
  authed = true,
  method: AxiosRequestConfig["method"] = "get",
  data: object = {},
  headers: Record<string, string> = {}
): Promise<T> {
  const token = await useApiToken(authed);
  return authed && !token
    ? Promise.reject({
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
      })
    : request(`internal/${url}`, token, method, data, headers);
}
