import type { AxiosRequestConfig, AxiosResponse } from "axios";
import axios from "axios";
import type { NuxtApp } from "nuxt/app";
import NProgress from "nprogress";
import { defineNuxtPlugin, useAuth, useRequestEvent } from "#imports";
import { useAuthStore } from "~/store/auth";
import { authLog, axiosLog } from "~/composables/useLog";
import { useConfig } from "~/composables/useConfig";
import { transformAxiosError } from "~/composables/useErrorHandling";

let progressBarTimeout: any;

const startProgressBar = () => {
  clearTimeout(progressBarTimeout);
  progressBarTimeout = setTimeout(NProgress.start, 400);
};

const stopProgressBar = () => {
  clearTimeout(progressBarTimeout);
  NProgress.done();
};

export default defineNuxtPlugin((nuxtApp: NuxtApp) => {
  const config = useConfig();
  const options: AxiosRequestConfig = {
    baseURL: import.meta.env.SSR ? config.proxyHost : config.publicHost,
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
      forwardRequestHeaders(config, nuxtApp);
      // axiosLog("calling with headers", config.headers);
      // Progress bar
      if (process.client) startProgressBar();
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
      // Progress bar
      if (process.client) stopProgressBar();
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

      // Progress bar
      if (process.client) stopProgressBar();

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
