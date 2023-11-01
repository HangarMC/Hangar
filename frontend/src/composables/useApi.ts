import type { AxiosError, AxiosRequestConfig } from "axios";
import qs from "qs";
import Cookies from "universal-cookie";
import { authLog, fetchLog } from "~/composables/useLog";
import { useAxios } from "~/composables/useAxios";
import { useCookie } from "#imports";

type FilteredAxiosConfig = Omit<AxiosRequestConfig, "method" | "url" | "data" | "params" | "baseURL">;

function request<T>(url: string, method: AxiosRequestConfig["method"], data: object | string, axiosOptions: FilteredAxiosConfig = {}): Promise<T> {
  return new Promise<T>((resolve, reject) => {
    return useAxios()
      .request<T>({
        method,
        url: `/api/${url}`,
        data: method?.toLowerCase() !== "get" ? data : {},
        params: method?.toLowerCase() === "get" ? data : {},
        ...axiosOptions,
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
            useCookie("hangar_stats", { path: "/", sameSite: "strict", maxAge: 60 * 60 * 24 * 356.24 * 1000 }).value = statCookie;
            authLog("got stats cookie from backend", statCookie);
          }
        }
        resolve(data);
      })
      .catch((error: AxiosError) => {
        const { trace, ...err } = (error.response?.data as { trace: any }) || {};
        authLog("failed", err);
        reject(error);
      });
  });
}

export function useApi<T>(url: string, method: AxiosRequestConfig["method"] = "get", data: object = {}, axiosOptions: FilteredAxiosConfig = {}): Promise<T> {
  fetchLog("useApi", url, data);
  return request(`v1/${url}`, method, data, axiosOptions);
}

export function useInternalApi<T = void>(
  url: string,
  method: AxiosRequestConfig["method"] = "get",
  data: object | string = {},
  axiosOptions: FilteredAxiosConfig = {}
): Promise<T> {
  fetchLog("useInternalApi", url, data);
  return request(`internal/${url}`, method, data, axiosOptions);
}
