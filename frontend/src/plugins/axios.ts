import type { AxiosRequestConfig, AxiosResponse } from "axios";
import axios from "axios";
import type { NuxtApp } from "nuxt/app";

export default defineNuxtPlugin((nuxtApp) => {
  const config = useRuntimeConfig();
  const options: AxiosRequestConfig = {
    baseURL: import.meta.client ? config.public.host : config.backendHost,
    timeout: 10_000,
  };
  axiosLog("axios options", options);
  const axiosInstance = axios.create(options);

  axiosInstance.interceptors.request.use(
    (config) => {
      const authStore = useAuthStore();
      // forward auth token
      addAuthHeader(config, authStore.token);
      // forward other headers for ssr
      if (import.meta.server) forwardRequestHeaders(config, nuxtApp as NuxtApp);
      // axiosLog("calling with headers", config.headers);
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  axiosInstance.interceptors.response.use(
    (res) => {
      // forward cookies and stuff to browser
      forwardResponseHeaders(res, nuxtApp as NuxtApp);
      return res;
    },
    async (err) => {
      const authStore = useAuthStore();
      const originalConfig = err.config as AxiosRequestConfig & { _retry: boolean };

      if (originalConfig?.url !== "/refresh" && originalConfig?.url !== "/invalidate" && err.response) {
        // token expired
        if (err.response.status === 403 && err.response.data?.message === "JWT was expired" && !originalConfig._retry) {
          originalConfig._retry = true;

          authLog("Request to", originalConfig.url, "failed with", err.response.status, err.response.data, "==> refreshing token");
          const refreshedToken = await useAuth.refreshToken();
          if (refreshedToken) {
            authStore.token = refreshedToken;
            authLog("redo request", originalConfig.url);
            addAuthHeader(originalConfig, refreshedToken);
            return axiosInstance(originalConfig);
          }
        }
      } else {
        axiosLog("got error", transformAxiosError(err));
      }

      throw err;
    }
  );

  return {
    provide: {
      axios: axiosInstance,
    },
  };
});

function addAuthHeader(config: AxiosRequestConfig, token: string | undefined | null) {
  if (!config.headers) {
    config.headers = {};
  }
  if (token) {
    config.headers.Authorization = "HangarAuth " + token;
  }
}

function forwardRequestHeaders(config: AxiosRequestConfig, nuxtApp: NuxtApp) {
  if (import.meta.client) return;
  const req = useRequestEvent(nuxtApp)?.node?.req;

  const forward = (header: string) => {
    if (req?.headers?.[header] && config.headers) {
      config.headers[header] = req.headers[header];
    }
  };

  forward("cf-connecting-ip");
  forward("cf-ipcountry");
  forward("x-forwarded-host");
  forward("x-real-ip");
  forward("cookie");
  forward("sec-ch-prefers-color-scheme");
  forward("sec-ch-ua-mobile");
  forward("sec-ch-ua-platform");
  forward("sec-ch-ua");
  forward("user-agent");
}

function forwardResponseHeaders(axiosResponse: AxiosResponse, nuxtApp: NuxtApp) {
  if (import.meta.client) return;
  const res = useRequestEvent(nuxtApp)?.node?.res;

  const forward = (header: string) => {
    if (axiosResponse.headers[header]) {
      res?.setHeader(header, axiosResponse.headers[header]);
    }
  };

  console.log("forward headers from", axiosResponse.request?.path);

  forward("set-cookie");
  forward("server");
}
