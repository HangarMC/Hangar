import NProgress from "nprogress";
import type { UserModule } from "~/types";
import { nextTick } from "vue";

export const install: UserModule = ({ isClient, router }) => {
  if (isClient) {
    router.beforeEach(() => {
      NProgress.start();
    });
    router.afterEach(async () => {
      await nextTick(() => NProgress.done());
    });
  }
};
