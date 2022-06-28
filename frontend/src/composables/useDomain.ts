import * as domain from "domain";
import { Context } from "vite-ssr/vue";
import { domainLog } from "~/lib/composables/useLog";

export function create(request: Context["request"], response: Context["response"]) {
  if (!import.meta.env.SSR) return null;
  domainLog("enter");
  const d = domain.create();
  d.add(request);
  d.add(response!);
  d.on("error", (err) => {
    domainLog("domain error!", err);
  });
  d.context = {};
  d.enter();
  set("req", request);
  set("res", response);
  return d;
}

export function exit(d: domain.Domain | null) {
  if (!import.meta.env.SSR || !d) return;
  d.context = null;
  d.exit();
  domainLog("exit");
}

export function set(key: string, value: unknown) {
  if (!import.meta.env.SSR) return;
  if (!domain.active) {
    throw new Error("no active domain found to set key " + key);
  }
  if (!domain.active.context) {
    throw new Error("no context found on domain to set key " + key);
  }
  domain.active.context[key] = value;
}

export function get<T>(key: string): T | null {
  if (!import.meta.env.SSR) return null;
  if (!domain.active) {
    throw new Error("no active domain found to get key " + key);
  }
  if (!domain.active.context) {
    throw new Error("no context found on domain to get key " + key);
  }
  return domain.active.context[key];
}

export function isActive() {
  return import.meta.env.SSR && domain.active && domain.active.context;
}
