import * as Sentry from "@sentry/nuxt";

function init() {
  const router = useRouter();
  const {
    public: { sentry },
  } = useRuntimeConfig();

  if (!sentry.dsn) {
    return;
  }

  Sentry.init({
    dsn: sentry.dsn,
    environment: sentry.environment,
    integrations: [Sentry.browserTracingIntegration({ router }), Sentry.piniaIntegration(usePinia(), {})],

    tracePropagationTargets: sentry.tracePropagationTargets,
    tracesSampleRate: sentry.tracesSampleRate,

    replaysSessionSampleRate: 0.1,
    replaysOnErrorSampleRate: 1,

    debug: sentry.debug,
  });
}

init();
