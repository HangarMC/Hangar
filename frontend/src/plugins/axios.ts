import axios, { AxiosRequestConfig, AxiosResponse } from "axios";
import { NuxtApp } from "nuxt/app";
import { defineNuxtPlugin, useAuth, useRequestEvent } from "#imports";
import { useAuthStore } from "~/store/auth";
import { authLog, axiosLog } from "~/lib/composables/useLog";
import { useConfig } from "~/lib/composables/useConfig";

export default defineNuxtPlugin((nuxtApp: NuxtApp) => {
  const config = useConfig();
  const options = {
    baseURL: import.meta.env.SSR ? config.proxyHost : config.publicHost,
  };
  axiosLog("axios options", options);
  const axiosInstance = axios.create(options);

  axiosInstance.interceptors.request.use(
    (config) => {
      const authStore = useAuthStore();
      const token = authStore.token;
      // forward auth token
      if (!config.headers) {
        config.headers = {};
      }
      if (token) {
        config.headers.Authorization = "HangarAuth " + token;
      }
      // forward other headers for ssr
      forwardRequestHeaders(config, nuxtApp);
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
      forwardResponseHeaders(res, nuxtApp);
      return res;
    },
    async (err) => {
      const authStore = useAuthStore();
      const originalConfig = err.config;

      const transformedError = {
        code: err?.code,
        requestUrl: err?.request?.path,
        status: err?.response?.status,
        data: err?.response?.data,
      };
      axiosLog("got error", transformedError);

      if (originalConfig?.url !== "/refresh" && originalConfig?.url !== "/invalidate" && err.response) {
        // token expired
        if (err.response.status === 401 && !originalConfig._retry) {
          originalConfig._retry = true;

          authLog("Request to", originalConfig.url, "failed with", err.response.status, "==> refreshing token");
          const refreshedToken = await useAuth.refreshToken();
          if (refreshedToken) {
            authStore.token = refreshedToken;
            return axiosInstance(originalConfig);
          }
        }
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

function forwardRequestHeaders(config: AxiosRequestConfig, nuxtApp: NuxtApp) {
  if (!process.server) return;
  const req = useRequestEvent(nuxtApp).node.req;

  const forward = (header: string) => {
    if (req.headers[header] && config.headers) {
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
  if (!process.server) return;
  const res = useRequestEvent(nuxtApp).node.res;

  const forward = (header: string) => {
    if (axiosResponse.headers[header]) {
      res.setHeader(header, axiosResponse.headers[header]);
    }
  };

  forward("set-cookie");
  forward("server");
}
