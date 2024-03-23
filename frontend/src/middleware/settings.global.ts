export default defineNuxtRouteMiddleware(async (to, from) => {
  if (!process.server || to.fullPath.includes("/@vite")) return;
  const event = useRequestEvent();
  const request = event?.node?.res;
  const response = event?.node?.res;

  useSettingsStore().loadSettingsServer(request, response);
});
