<script lang="ts" setup>
import type { SettingsResponse } from "#shared/types/backend";

defineProps<{
  settings?: SettingsResponse;
}>();

const i18n = useI18n();
const settingsStore = useSettingsStore();

const themeModes = [
  { value: "system", text: "auth.settings.misc.systemTheme" },
  { value: "light", text: "auth.settings.misc.lightTheme" },
  { value: "dark", text: "auth.settings.misc.darkTheme" },
] as const;

const accentColors = [
  { value: "blue", text: "Blue", color: "#004ee9" },
  { value: "red", text: "Red", color: "#f44336" },
  { value: "pink", text: "Pink", color: "#e91e63" },
  { value: "purple", text: "Purple", color: "#9c27b0" },
  { value: "deep-purple", text: "Deep purple", color: "#673ab7" },
  { value: "indigo", text: "Indigo", color: "#3f51b5" },
  { value: "light-blue", text: "Light blue", color: "#03a9f4" },
  { value: "cyan", text: "Cyan", color: "#00bcd4" },
  { value: "teal", text: "Teal", color: "#009688" },
  { value: "green", text: "Green", color: "#4caf50" },
  { value: "light-green", text: "Light green", color: "#8bc34a" },
  { value: "lime", text: "Lime", color: "#cddc39" },
  { value: "yellow", text: "Yellow", color: "#ffeb3b" },
  { value: "amber", text: "Amber", color: "#ffc107" },
  { value: "orange", text: "Orange", color: "#ff9800" },
  { value: "deep-orange", text: "Deep orange", color: "#ff5722" },
  { value: "brown", text: "Brown", color: "#795548" },
  { value: "gray", text: "Gray", color: "#9e9e9e" },
];
const accentColor = useAccentColor();

const locale = ref(i18n.locale.value);
const languages = (useRuntimeConfig().public.i18n.locales as { code: string; name: string }[]).map((locale) => ({
  value: locale.code,
  text: locale.name,
}));

watch(locale, (newLocale) => {
  i18n.locale.value = newLocale;
  window.location.reload();
});
</script>

<template>
  <div>
    <PageTitle>{{ i18n.t("auth.settings.misc.header") }}</PageTitle>
    <p class="-mt-3 text-sm text-gray-secondary">{{ i18n.t("auth.settings.misc.description") }}</p>

    <section class="mt-5">
      <h3 class="font-semibold">{{ i18n.t("auth.settings.misc.theme") }}</h3>
      <div class="mt-2 inline-flex rounded-lg background-card p-1">
        <button
          v-for="mode in themeModes"
          :key="mode.value"
          type="button"
          class="rounded-md px-3 py-1.5 text-sm font-semibold transition-colors"
          :class="
            settingsStore.themeMode === mode.value
              ? 'background-default color-primary shadow-sm'
              : 'text-gray-secondary hover:(text-black dark:text-white)'
          "
          :aria-pressed="settingsStore.themeMode === mode.value"
          @click="settingsStore.setThemeMode(mode.value)"
        >
          {{ i18n.t(mode.text) }}
        </button>
      </div>
    </section>

    <section class="mt-5">
      <h3 class="font-semibold">{{ i18n.t("auth.settings.misc.accentColor") }}</h3>
      <div class="mt-2 flex flex-wrap gap-2">
        <button
          v-for="color in accentColors"
          :key="color.value"
          type="button"
          class="h-8 w-8 rounded-lg border-2 transition-transform hover:scale-105 focus-visible:(outline-2 outline-offset-2 outline-primary-500)"
          :class="accentColor === color.value ? 'border-gray-900 ring-2 ring-gray-400 dark:border-white' : 'border-transparent'"
          :style="{ backgroundColor: color.color }"
          :title="color.text"
          :aria-label="color.text"
          :aria-pressed="accentColor === color.value"
          @click="accentColor = color.value"
        />
      </div>
      <p class="mt-2 text-sm text-gray-secondary">{{ i18n.t("auth.settings.misc.accentDescription") }}</p>
    </section>

    <section class="mt-5">
      <h3 class="font-semibold mb-2">{{ i18n.t("auth.settings.misc.language") }}</h3>
      <InputSelect v-model="locale" class="w-full" :values="languages" :aria-label="i18n.t('auth.settings.misc.language')" />
      <p class="mt-1 text-sm text-gray-secondary">{{ i18n.t("auth.settings.misc.languageDescription") }}</p>
    </section>
  </div>
</template>
