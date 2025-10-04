import * as Sentry from "@sentry/nuxt";
import { SEMANTIC_ATTRIBUTE_SENTRY_SOURCE } from "@sentry/nuxt";

// this overrides the default sentry nuxt integration to look nicer
export default defineNuxtPlugin((nitro) => {
  nitro.hooks.hook("app:rendered", (ctx) => {
    const activeSpan = Sentry.getActiveSpan();
    const route = ctx.ssrContext?.nuxt._route;
    if (activeSpan && route) {
      const rootSpan = Sentry.getRootSpan(activeSpan);
      if (!rootSpan) {
        return;
      }
      rootSpan.setAttributes({
        [SEMANTIC_ATTRIBUTE_SENTRY_SOURCE]: "route",
        "http.route": route.name,
        "segment.name": "Route " + route.name,
        "span.description":
          "Route Rendered: " +
          route.name +
          " " +
          (route.params
            ? Object.keys(route.params)
                .map((key) => `${key}=${route.params[key as never]}`)
                .join(", ")
            : ""),
      });
    }
  });
});
