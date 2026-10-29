<script lang="ts" setup>
import type { Platform, Version, PluginDependency } from "#shared/types/backend";

const props = defineProps<{
  pluginDependencies: Version["pluginDependencies"];
  platform: Platform;
}>();

const { t } = useI18n();

type EditableDependency = PluginDependency & { id: string; mode: "file" | "url" };

let nextId = 0;
const dependencies = ref<EditableDependency[]>([]);

watch(
  () => props.pluginDependencies[props.platform],
  (deps) => (dependencies.value = (deps ?? []).map((dep) => ({ ...dep, id: `dep-${nextId++}`, mode: dep.externalUrl ? "url" : "file" }))),
  { immediate: true }
);

// the backend keys dependencies by name; a repeat fails the whole save
const duplicateNames = computed(() => {
  const counts = new Map<string, number>();
  for (const dep of dependencies.value) {
    const key = dep.name?.trim().toLowerCase();
    if (key) counts.set(key, (counts.get(key) ?? 0) + 1);
  }
  return new Set([...counts].filter(([, count]) => count > 1).map(([key]) => key));
});
const hasDuplicates = computed(() => duplicateNames.value.size > 0);

function isDuplicate(dep: EditableDependency) {
  const key = dep.name?.trim().toLowerCase();
  return !!key && duplicateNames.value.has(key);
}

function add() {
  dependencies.value.push({
    platform: props.platform,
    name: "",
    required: true,
    mode: "file",
    id: `dep-${nextId++}`,
    externalUrl: undefined,
    projectId: -1,
  });
}

function remove(index: number) {
  dependencies.value.splice(index, 1);
}

function reset() {
  dependencies.value.length = 0;
}

defineExpose({ dependencies, hasDuplicates, reset });
</script>

<template>
  <div>
    <ul v-if="dependencies.length > 0" class="mb-3 flex flex-col gap-2">
      <DependencyEditorRow
        v-for="(dep, index) in dependencies"
        :key="`${platform}-${dep.id}`"
        v-model="dependencies[index]!"
        :idx="index"
        :duplicate="isDuplicate(dep)"
        @delete="remove(index)"
      />
    </ul>
    <div v-else class="mb-3 rounded-md border border-dashed border-gray-300 px-4 py-6 text-center dark:border-gray-700">
      <p class="font-semibold">{{ t("version.deps.empty") }}</p>
      <p class="mt-0.5 text-sm text-gray-secondary">{{ t("version.deps.emptyHint") }}</p>
    </div>

    <div class="flex flex-wrap items-center gap-x-3 gap-y-2">
      <Button variant="outline" tone="neutral" size="sm" @click="add">
        <IconMdiPlus />
        {{ t("version.deps.add") }}
      </Button>
      <span v-if="hasDuplicates" class="text-sm text-red-500 dark:text-red-400">{{ t("version.deps.duplicateHint") }}</span>
    </div>
  </div>
</template>
