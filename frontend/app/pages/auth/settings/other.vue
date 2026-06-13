<script lang="ts" setup>
import type { SettingsResponse } from "#shared/types/backend";

defineProps<{
  settings?: SettingsResponse;
}>();

const i18n = useI18n();

const accentColors = [
  { value: "blue", text: "Blue", color: "#004ee9" },
  { value: "red", text: "Red", color: "#f44336" },
  { value: "pink", text: "Pink", color: "#e91e63" },
  { value: "purple", text: "Purple", color: "#9c27b0" },
  { value: "deep-purple", text: "Deep Purple", color: "#673ab7" },
  { value: "indigo", text: "Indigo", color: "#3f51b5" },
  { value: "light-blue", text: "Light Blue", color: "#03a9f4" },
  { value: "cyan", text: "Cyan", color: "#00bcd4" },
  { value: "teal", text: "Teal", color: "#009688" },
  { value: "green", text: "Green", color: "#4caf50" },
  { value: "light-green", text: "Light Green", color: "#8bc34a" },
  { value: "lime", text: "Lime", color: "#cddc39" },
  { value: "yellow", text: "Yellow", color: "#ffeb3b" },
  { value: "amber", text: "Amber", color: "#ffc107" },
  { value: "orange", text: "Orange", color: "#ff9800" },
  { value: "deep-orange", text: "Deep Orange", color: "#ff5722" },
  { value: "brown", text: "Brown", color: "#795548" },
  { value: "gray", text: "Gray", color: "#71717a" },
];
const accentColor = useAccentColor();

const locale = ref(i18n.locale);
const languages = (useRuntimeConfig().public.i18n.locales as { code: string; name: string }[]).map((locale) => ({
  value: locale.code,
  text: locale.name,
}));
const selectedLanguage = computed(() => languages.find((language) => language.value === locale.value));

watch(locale, async (newLocale) => {
  i18n.locale.value = newLocale;
  window.location.reload();
});
</script>

<template>
  <div class="min-w-0">
    <div class="grid grid-cols-1 items-start gap-4 lg:grid-cols-2">
      <Card>
        <div class="flex items-start gap-3">
          <span class="inline-flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-md bg-gray-100 text-xl dark:bg-charcoal-500">
            <IconMdiPaletteOutline />
          </span>
          <div>
            <h2 class="text-xl font-bold">{{ i18n.t("auth.settings.misc.accentColor") }}</h2>
            <p class="mt-1 text-sm text-gray">Choose the accent color used for links, buttons, and selected controls.</p>
          </div>
        </div>
        <div
          class="mt-4 flex items-start gap-2 rounded-lg border border-gray-200 bg-gray-100/60 px-3 py-2 text-sm dark:border-gray-800 dark:bg-charcoal-500/60"
        >
          <IconMdiInformationOutline class="mt-0.5 flex-shrink-0 text-gray" />
          <span>{{ i18n.t("auth.settings.misc.alert.colorAlert") }}</span>
        </div>
        <div class="mt-4 flex flex-wrap gap-2">
          <button
            v-for="color in accentColors"
            :key="color.value"
            class="relative inline-flex h-9 w-9 items-center justify-center rounded-md border-2 transition-transform"
            :class="accentColor === color.value ? 'border-white shadow-md dark:border-white' : 'border-transparent'"
            :style="{ backgroundColor: color.color }"
            :title="color.text"
            :aria-label="`Use ${color.text} accent color`"
            :aria-pressed="accentColor === color.value"
            @click="accentColor = color.value"
          >
            <IconMdiCheck v-if="accentColor === color.value" class="text-lg text-white drop-shadow" />
          </button>
        </div>
      </Card>

      <Card>
        <div class="flex items-start gap-3">
          <span class="inline-flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-md bg-gray-100 text-xl dark:bg-charcoal-500">
            <IconMdiTranslate />
          </span>
          <div>
            <h2 class="text-xl font-bold">{{ i18n.t("auth.settings.misc.language") }}</h2>
            <p class="mt-1 text-sm text-gray">Select the language used throughout Hangar.</p>
          </div>
        </div>
        <div
          class="mt-4 flex items-start gap-2 rounded-lg border border-gray-200 bg-gray-100/60 px-3 py-2 text-sm dark:border-gray-800 dark:bg-charcoal-500/60"
        >
          <IconMdiInformationOutline class="mt-0.5 flex-shrink-0 text-gray" />
          <span>{{ i18n.t("auth.settings.misc.alert.languageAlert") }}</span>
        </div>
        <div class="mt-4">
          <DropdownButton button-size="medium" button-type="transparent" match-width spread-arrow>
            <template #button-label>
              <div class="flex w-44 items-center justify-start gap-2">
                <IconMdiWeb />
                <span class="truncate">{{ selectedLanguage?.text }}</span>
              </div>
            </template>
            <template #default="{ close }">
              <DropdownItem
                v-for="language in languages"
                :key="language.value"
                :style="
                  locale === language.value
                    ? {
                        backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
                        borderColor: 'var(--primary-500)',
                      }
                    : {}
                "
                @click="
                  locale = language.value;
                  close();
                "
              >
                {{ language.text }}
              </DropdownItem>
            </template>
          </DropdownButton>
        </div>
      </Card>
    </div>
  </div>
</template>
