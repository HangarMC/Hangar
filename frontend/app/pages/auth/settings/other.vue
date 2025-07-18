<script lang="ts" setup>
import type { SettingsResponse } from "#shared/types/backend";

defineProps<{
  settings?: SettingsResponse;
}>();

const i18n = useI18n();

const accentColors = [
  { value: "blue", text: "Blue" },
  { value: "red", text: "Red" },
  { value: "pink", text: "Pink" },
  { value: "purple", text: "Purple" },
  { value: "deep-purple", text: "DeepPurple" },
  { value: "indigo", text: "Indigo" },
  { value: "light-blue", text: "LightBlue" },
  { value: "cyan", text: "Cyan" },
  { value: "teal", text: "Teal" },
  { value: "green", text: "Green" },
  { value: "light-green", text: "LightGreen" },
  { value: "lime", text: "Lime" },
  { value: "yellow", text: "Yellow" },
  { value: "amber", text: "Amber" },
  { value: "orange", text: "Orange" },
  { value: "deep-orange", text: "DeepOrange" },
  { value: "brown", text: "Brown" },
  { value: "gray", text: "Gray" },
];
const accentColor = useAccentColor();

const locale = ref(i18n.locale);
const languages = (useRuntimeConfig().public.i18n.locales as { code: string; name: string }[]).map((locale) => ({
  value: locale.code,
  text: locale.name,
}));

watch(locale, async (newLocale) => {
  i18n.locale.value = newLocale;
  window.location.reload();
});
</script>

<template>
  <div>
    <PageTitle>{{ i18n.t("auth.settings.misc.header") }}</PageTitle>
    <Alert type="warning" class="mb-4">{{ i18n.t("auth.settings.misc.alert.colorAlert") }}</Alert>
    <InputSelect v-model="accentColor" :values="accentColors" :label="i18n.t('auth.settings.misc.accentColor')" />

    <Alert type="warning" class="my-4">{{ i18n.t("auth.settings.misc.alert.languageAlert") }}</Alert>
    <InputSelect v-model="locale" :values="languages" :label="i18n.t('auth.settings.misc.language')" />
  </div>
</template>
