// import * as Sentry from "@sentry/node";
// import { nodeProfilingIntegration } from "@sentry/profiling-node";
// import { H3Error } from "h3";

export default defineNitroPlugin((nitroApp) => {
  const {
    public: { sentry },
  } = useRuntimeConfig();

  if (!sentry.dsn) {
    console.warn("Sentry DSN not set, skipping Sentry initialization");
    return;
  }

  // TODO make this work with bun

  // let profiler = null;
  // if (typeof Bun === "undefined") {
  //   // profiler = nodeProfilingIntegration();
  // }

  // Sentry.init({
  //   dsn: sentry.dsn,
  //   environment: sentry.environment,
  //   integrations: profiler ? [profiler] : [],
  //   tracesSampleRate: 1,
  //   profilesSampleRate: 0.2,
  // });

  // nitroApp.hooks.hook("error", (error) => {
  //   // Do not handle 404s and 422s
  //   if (error instanceof H3Error) {
  //     if (error.statusCode === 404 || error.statusCode === 422) {
  //       return;
  //     }
  //   }
  //
  //   Sentry.captureException(error);
  // });
  //
  // nitroApp.hooks.hook("request", (event) => {
  //   event.context.$sentry = Sentry;
  // });
  //
  // nitroApp.hooks.hookOnce("close", async () => {
  //   await Sentry.close(2000);
  // });
});
