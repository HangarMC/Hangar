import { joinURL } from "ufo";

export default defineEventHandler(async (event) => {
  const base = useRuntimeConfig().backendHost;
  const target = joinURL(base, event.path);
  return proxyRequest(event, target, { headers: { "Accept-Encoding": "gzip", Origin: base } });
});
