import NProgress from "nprogress";

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
  if (!import.meta.server) {
    useRouter().beforeEach(() => {
      startProgressBar();
    });
    useRouter().afterEach(async () => {
      await nextTick(() => stopProgressBar());
    });
  }
});
