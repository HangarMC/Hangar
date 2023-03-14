import NProgress from "nprogress";
import { nextTick } from "vue";
import { defineNuxtPlugin, useRouter } from "#imports";

let progressBarTimeout: any;

const startProgressBar = () => {
  clearTimeout(progressBarTimeout);
  progressBarTimeout = setTimeout(NProgress.start, 400);
};

const stopProgressBar = () => {
  clearTimeout(progressBarTimeout);
  NProgress.done();
};

export default defineNuxtPlugin(() => {
  if (!process.server) {
    useRouter().beforeEach(() => {
      startProgressBar();
    });
    useRouter().afterEach(async () => {
      await nextTick(() => stopProgressBar());
    });
  }
});
