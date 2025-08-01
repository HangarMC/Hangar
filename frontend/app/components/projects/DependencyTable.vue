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
  <Table v-if="dependencies.length > 0" class="mb-2">
    <thead>
      <tr>
        <th>{{ t("version.new.form.linkOrProject") }}</th>
        <th>{{ t("general.name") }}</th>
        <th>{{ t("general.required") }}</th>
        <th v-if="!noEditing" />
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
  <div v-if="!noEditing" class="m-2" :class="dependencies.length > 0 ? '-mt-2' : ''">
    <Button block @click="addDep">
      <IconMdiPlus />
      {{ t("general.add") }}
    </Button>
  </div>
</template>
