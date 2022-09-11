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
  "papermc.dev": {
    publicHost: "https://hangar.papermc.dev",
    proxyHost: "http://hangar-backend:8080",
    authHost: "https://hangarauth.papermc.dev",
    paypalEnv: "production",
    paypalIpn: "https://hangar.papermc.dev/api/internal/paypal/ipn",
  },
  "papermc.io": {
    publicHost: "https://hangar.papermc.io",
    proxyHost: "http://hangar-backend:8080",
    authHost: "https://hangarauth.papermc.io",
    paypalEnv: "production",
    paypalIpn: "https://hangar.papermc.io/api/internal/paypal/ipn",
  },
};

export function useConfig(): Config {
  let key: string;
  if (import.meta.env.SSR) {
    key = process.env.HANGAR_CONFIG_ENV || "localhost";
  } else {
    const split = location.hostname.split(".");
    if (split.length === 1) {
      key = split[0];
    } else if (split.length === 3) {
      key = split[1] + "." + split[2];
    } else {
      key = location.hostname;
    }
  }
  configLog("useConfig", key);
  return configs[key];
}
