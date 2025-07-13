import * as Sentry from "@sentry/nuxt";
import { bunServerIntegration } from "@sentry/bun";

function init() {
  if (!process.env.NUXT_PUBLIC_SENTRY_DSN) {
    console.warn("Sentry DSN not set, skipping Sentry initialization");
    return;
  }

  Sentry.init({
    dsn: process.env.NUXT_PUBLIC_SENTRY_DSN,
    environment: process.env.NUXT_PUBLIC_SENTRY_ENVIRONMENT || "local",

    integrations: typeof Bun === "undefined" ? [] : [bunServerIntegration()],

    tracesSampleRate: Number(process.env.NUXT_PUBLIC_SENTRY_TRACES_SAMPLE_RATE) || 1,
    tracePropagationTargets: [
      "http://localhost:3333",
      "https://hangar.papermc.dev",
      "https://hangar.papermc.io",
      "http://hangar-backend:8080",
      "http://localhost:8080",
    ],

    debug: process.env.NUXT_PUBLIC_SENTRY_DEBUG === "true",
  });
}

init();
