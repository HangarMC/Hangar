import NProgress from "nprogress";
import { nextTick } from "vue";
import { NuxtApp } from "nuxt/app";
import { defineNuxtPlugin } from "#imports";

let progressBarTimeout: any;

const startProgressBar = () => {
  clearTimeout(progressBarTimeout);
  progressBarTimeout = setTimeout(NProgress.start, 350);
};

const stopProgressBar = () => {
  clearTimeout(progressBarTimeout);
  NProgress.done();
};

export default defineNuxtPlugin((nuxtApp: NuxtApp) => {
  if (!process.server) {
    nuxtApp.$router.beforeEach(() => {
      startProgressBar();
    });
    nuxtApp.$router.afterEach(async () => {
      await nextTick(() => stopProgressBar());
    });
  }
});
