export default function useTracking(elementName: MaybeRefOrGetter<string>, additionalData: MaybeRefOrGetter<Record<string, MaybeRefOrGetter<unknown>>> = {}) {
  return {
    click: (e: MouseEvent) => track("click", elementName, additionalData),
    change: (e: MouseEvent) => track("change", elementName, additionalData),
  };
}

export function track(type: string, elementName: MaybeRefOrGetter<string>, additionalData: MaybeRefOrGetter<Record<string, MaybeRefOrGetter<unknown>>> = {}) {
  if (import.meta.server) return;
  const name = toValue(elementName);
  const data: Record<string, unknown> = { type };
  const entries = Object.entries(toValue(additionalData));
  for (const [k, v] of entries) {
    data[k] = toValue(v);
  }
  data["type"] = type;
  window.umami?.track(name, data);
}

export function identify() {
  if (import.meta.server) return;
  const authStore = useAuthStore();
  const settingsStore = useSettingsStore();
  const i18n = useNuxtApp().$i18n;

  const props: Record<string, unknown> = {
    user: authStore.user ? authStore.user.name : "<anonymous>",
    theme: settingsStore.darkMode ? "dark" : "light",
    language: i18n.locale.value,
  };
  window.umami?.identify(props);
}
