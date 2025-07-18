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
  for (const [k, v] of Object.entries(toValue(additionalData))) {
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

  const props: Record<string, unknown> = {};
  props.user = authStore.user ? authStore.user.name : "<anonymous>";
  props.theme = settingsStore.darkMode ? "dark" : "light";
  props.language = i18n.locale.value;
  window.umami?.identify(props);
}
