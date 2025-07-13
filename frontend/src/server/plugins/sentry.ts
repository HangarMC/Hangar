import * as Sentry from "@sentry/nuxt";

export default defineNitroPlugin((nitroApp) => {
  const {
    public: { sentry },
  } = useRuntimeConfig();

  if (!sentry.dsn) {
    console.warn("Sentry DSN not set, skipping Sentry initialization");
    return;
  }

  Sentry.init({
    dsn: sentry.dsn,
    environment: sentry.environment,

    tracesSampleRate: sentry.tracesSampleRate,
    tracePropagationTargets: sentry.tracePropagationTargets,

    debug: sentry.debug,
  });
});
