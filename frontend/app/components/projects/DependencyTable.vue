<script lang="ts" setup>
import type { Platform, Version, PluginDependency } from "#shared/types/backend";
import DependencyTableRow from "~/components/projects/DependencyTableRow.vue";

const i18n = useI18n();
const t = i18n.t;

const props = withDefaults(
  defineProps<{
    pluginDependencies: Version["pluginDependencies"];
    platform: Platform;
    noEditing?: boolean;
  }>(),
  {
    noEditing: false,
  }
);

const dependencies = ref<(PluginDependency & { id?: string; mode: "file" | "url" })[]>([]);

watch(
  () => props.pluginDependencies[props.platform],
  (newVal) => (dependencies.value = newVal ? [...newVal].map((dep) => ({ ...dep, id: "id" + Math.random(), mode: dep.externalUrl ? "url" : "file" })) : []),
  { immediate: true }
);

function addDep() {
  dependencies.value.push({
    platform: props.platform,
    name: "",
    required: true,
    mode: "file",
    id: "id" + Math.random(),
    externalUrl: undefined,
    projectId: -1,
  });
}

function deleteDep(index: number) {
  dependencies.value.splice(index, 1);
}

function reset() {
  dependencies.value.splice(0);
}

defineExpose({ dependencies, reset });
</script>

<template>
  <div>
    <table class="w-full table-fixed">
      <thead class="text-left text-xs font-semibold text-gray">
        <tr class="border-b dark:border-gray-800">
          <th class="w-30 px-2 py-2.5">Source</th>
          <th class="px-3 py-2.5">Dependency</th>
          <th class="w-32 px-2 py-2.5">Name</th>
          <th class="w-20 px-2 py-2.5">Required</th>
          <th v-if="!noEditing" class="w-12 px-2 py-2.5" />
        </tr>
      </thead>
      <tbody>
        <DependencyTableRow
          v-for="(dep, index) in dependencies"
          :key="`${platform}-${dep.id}`"
          v-model="dependencies[index]!"
          :idx="index"
          :no-editing="noEditing"
          @delete="deleteDep(index)"
        />
        <tr v-if="dependencies.length === 0">
          <td :colspan="noEditing ? 4 : 5" class="px-4 py-8 text-center">
            <IconMdiLinkVariant class="mx-auto mb-2 text-2xl text-gray" />
            <p class="font-semibold">No dependencies</p>
            <p class="mt-1 text-sm text-gray">Add a Hangar project or an external plugin URL.</p>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-if="!noEditing" class="border-t p-2 dark:border-gray-800">
      <Button button-type="secondary" size="medium" class="w-full" @click="addDep">
        <IconMdiPlus class="mr-1" />
        {{ t("general.add") }} dependency
      </Button>
    </div>
  </div>
</template>
