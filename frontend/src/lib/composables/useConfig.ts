import { configLog } from "~/lib/composables/useLog";

interface Config {
  publicHost: string;
  proxyHost: string;
  authHost: string;
  paypalEnv: string;
  paypalIpn: string;
}

const configs: Record<string, Config> = {
  localhost: {
    publicHost: "http://localhost:3333",
    proxyHost: "http://localhost:8080",
    authHost: "http://localhost:3001",
    paypalEnv: "sandbox",
    paypalIpn: "https://hangar.benndorf.dev/api/internal/paypal/ipn",
  },
  "hangar.test": {
    publicHost: "https://hangar.test",
    proxyHost: "http://hangar-backend:8080",
    authHost: "https://auth.hangar.test",
    paypalEnv: "production",
    paypalIpn: "https://hangar.benndorf.dev/api/internal/paypal/ipn",
  },
};

export function useConfig(): Config {
  const key = import.meta.env.SSR ? process.env.HANGAR_CONFIG_ENV || "localhost" : location.hostname.replace("auth.", "");
  configLog("useConfig", key);
  return configs[key];
}
