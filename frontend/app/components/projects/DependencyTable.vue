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
  dependencies.value.length = 0;
}

defineExpose({ dependencies, reset });
</script>

<template>
  <Table v-if="dependencies.length > 0" class="mb-2">
    <thead>
      <tr>
        <th class="w-36" />
        <th>{{ t("version.new.form.linkOrProject") }}</th>
        <th>{{ t("general.name") }}</th>
        <th class="!text-center w-20">{{ t("general.required") }}</th>
        <th v-if="!noEditing" class="w-12" />
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
    </tbody>
  </Table>
  <p v-else-if="!noEditing" class="mb-3 text-sm text-gray-secondary">{{ t("version.page.noDependencies") }}</p>
  <Button v-if="!noEditing" variant="outline" tone="neutral" size="sm" @click="addDep">
    <IconMdiPlus />
    {{ t("general.add") }}
  </Button>
</template>
