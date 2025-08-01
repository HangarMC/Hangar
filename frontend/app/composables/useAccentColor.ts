export function useAccentColor() {
  const accentColor = useCookie("accent-color", {
    path: "/",
    maxAge: 60 * 60 * 24 * 7 * 365,
  });

  if (!accentColor.value) {
    accentColor.value = "blue";
  }

  const themeClass = computed(() => {
    return "theme-" + accentColor.value;
  });

  useHead({
    htmlAttrs: { class: themeClass },
  });

  return accentColor;
}
