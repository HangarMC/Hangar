import { joinURL } from "ufo";

export default defineEventHandler(async (event) => {
  const base = useRuntimeConfig().backendHost;
  const target = joinURL(base, event.path);
  // Hand redirects back to the browser
  return proxyRequest(event, target, {
    headers: { "Accept-Encoding": "gzip", Origin: base },
    fetchOptions: { redirect: "manual" },
  });
});
