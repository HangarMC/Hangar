export default defineNuxtRouteMiddleware(async (to, from) => {
  if (!process.server || to.fullPath.includes("/@vite")) return;
  const event = useRequestEvent();
  if (!event) return;
  await useSettingsStore().loadSettingsServer(event);
});
