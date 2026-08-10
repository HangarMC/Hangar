<script lang="ts" setup>
import type { PluginDependency } from "#shared/types/backend";
import { validUrl } from "~/composables/useValidationHelpers";

const props = defineProps<{
  idx: number;
  duplicate?: boolean;
}>();

const emit = defineEmits<{
  (e: "delete"): void;
}>();

const dep = defineModel<PluginDependency & { mode: "url" | "file" }>({ required: true });

const { t } = useI18n();
const route = useRoute("user-project");

const modeOptions: { value: "file" | "url"; label: string }[] = [
  { value: "file", label: t("version.deps.hangar") },
  { value: "url", label: t("version.deps.url") },
];

watch(
  () => dep.value.mode,
  (mode) => {
    if (mode === "file") dep.value.externalUrl = undefined;
  }
);

const target = computed(() => {
  if (dep.value.mode === "url") return dep.value.externalUrl || undefined;
  return dep.value.name ? "/api/internal/projects/project-redirect/" + dep.value.name : undefined;
});
</script>

<template>
  <li class="rounded-md border p-3 transition-colors" :class="duplicate ? 'border-red-400 dark:border-red-400' : 'border-gray-300 dark:border-gray-700'">
    <div class="flex flex-wrap items-center gap-2">
      <SegmentedControl v-model="dep.mode" :options="modeOptions" :aria-label="t('version.deps.kind')" class="flex-shrink-0" />

      <div class="min-w-45 flex-1">
        <ProjectSearchInput
          v-if="dep.mode === 'file'"
          v-model="dep.name"
          :label="t('version.new.form.hangarProject')"
          :rules="[required(t('version.new.form.hangarProject'))]"
          :name="'hangarproject-' + idx"
          :exclude-owner="route.params.user"
          :exclude-slug="route.params.project"
        />
        <InputText v-else v-model.trim="dep.name" :label="t('general.name')" :rules="[required(t('general.name'))]" :name="'name-' + idx" />
      </div>

      <Button
        :variant="dep.required ? 'solid' : 'outline'"
        :tone="dep.required ? 'primary' : 'neutral'"
        size="sm"
        class="min-w-21 flex-shrink-0"
        :aria-pressed="dep.required"
        :title="t(dep.required ? 'version.deps.makeOptional' : 'version.deps.makeRequired')"
        @click="dep.required = !dep.required"
      >
        {{ t(dep.required ? "general.required" : "general.optional") }}
      </Button>

      <div class="flex flex-shrink-0 items-center">
        <Button
          v-if="target"
          variant="ghost"
          tone="neutral"
          size="sm"
          icon-only
          :href="target"
          target="_blank"
          :title="t(dep.mode === 'url' ? 'version.deps.openLink' : 'version.deps.openProject')"
          :aria-label="t(dep.mode === 'url' ? 'version.deps.openLink' : 'version.deps.openProject')"
        >
          <IconMdiOpenInNew />
        </Button>
        <Button variant="ghost" tone="danger" size="sm" icon-only :title="t('general.delete')" :aria-label="t('general.delete')" @click="emit('delete')">
          <IconMdiDelete />
        </Button>
      </div>
    </div>

    <div v-if="dep.mode === 'url'" class="mt-2">
      <InputText
        v-model.trim="dep.externalUrl"
        :label="t('version.new.form.externalUrl')"
        :rules="[required(t('version.new.form.externalUrl')), validUrl()]"
        clearable
        :name="'externalurl-' + idx"
      />
    </div>

    <p v-if="duplicate" class="mt-2 text-sm text-red-500 dark:text-red-400">{{ t("version.deps.duplicate") }}</p>
  </li>
</template>
