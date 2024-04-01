export default defineNuxtRouteMiddleware((to, from) => {
  if (!process.server || to.fullPath.includes("/@vite")) return;
  const event = useRequestEvent();

  useSettingsStore().loadSettingsServer(event);
});
