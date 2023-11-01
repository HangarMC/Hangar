import type { RouteLocationNormalized } from "vue-router";
import { defineNuxtRouteMiddleware, useRequestEvent } from "#imports";
import { useSettingsStore } from "~/store/useSettingsStore";

export default defineNuxtRouteMiddleware(async (to: RouteLocationNormalized, from: RouteLocationNormalized) => {
  if (!process.server || to.fullPath.includes("/@vite")) return;
  const event = useRequestEvent();
  const request = event.node.res;
  const response = event.node.res;

  await useSettingsStore().loadSettingsServer(request, response);
});
