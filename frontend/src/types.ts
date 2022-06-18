import type { Context } from "vite-ssr/vue";
import type { App } from "vue";
import type { Router, RouteRecord } from "vue-router";

export type UserModule = (
  ctx: Context & {
    app: App;
    router: Router;
    initialRoute: RouteRecord;
  }
) => void;
