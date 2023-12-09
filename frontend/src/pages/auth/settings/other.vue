<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useAccentColor } from "~/composables/useAccentColor";
import PageTitle from "~/components/design/PageTitle.vue";
import InputSelect from "~/components/ui/InputSelect.vue";

const { t } = useI18n();

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
</script>

<template>
  <div>
    <PageTitle>{{ t("auth.settings.misc.header") }}</PageTitle>
    <InputSelect v-model="accentColor" :values="accentColors" :label="t('auth.settings.misc.accentColor')" />
  </div>
</template>
