import { configLog } from "~/composables/useLog";

interface Config {
  publicHost: string;
  proxyHost: string;
  paypalEnv: string;
  paypalIpn: string;
}

const configs: Record<string, Config> = {
  localhost: {
    publicHost: "http://localhost:3333",
    proxyHost: "http://localhost:8080",
    paypalEnv: "sandbox",
    paypalIpn: "https://hangar.benndorf.dev/api/internal/paypal/ipn",
  },
  "hangar.test": {
    publicHost: "https://hangar.test",
    proxyHost: "http://hangar-backend:8080",
    paypalEnv: "production",
    paypalIpn: "https://hangar.benndorf.dev/api/internal/paypal/ipn",
  },
  "papermc.dev": {
    publicHost: "https://hangar.papermc.dev",
    proxyHost: "http://hangar-backend:8080",
    paypalEnv: "production",
    paypalIpn: "https://hangar.papermc.dev/api/internal/paypal/ipn",
  },
  "papermc.io": {
    publicHost: "https://hangar.papermc.io",
    proxyHost: "http://hangar-backend:8080",
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

    if (key.startsWith("192.168")) {
      const local = configs.localhost;
      local.publicHost = local.publicHost.replace("http://localhost", "https://" + key);
      local.proxyHost = local.proxyHost.replace("http://localhost", "https://" + key);
      configLog("useConfig", key);
      return local;
    } else if (key.endsWith(".ngrok.io")) {
      const local = configs.localhost;
      local.publicHost = local.publicHost.replace("http://localhost", "https://" + key);
      local.proxyHost = local.proxyHost.replace("http://localhost", "https://" + key);
      configLog("useConfig", key);
      return local;
    }
  }
  configLog("useConfig", key);
  return configs[key];
}
