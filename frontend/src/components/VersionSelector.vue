<script lang="ts" setup>
import { computed, watch } from "vue";
import { PlatformVersion } from "hangar-internal";
import InputCheckbox from "~/lib/components/ui/InputCheckbox.vue";
import ArrowSpoiler from "~/lib/components/design/ArrowSpoiler.vue";

const props = defineProps<{
  versions: PlatformVersion[];
  modelValue: string[];
  open: boolean;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", selected: string[]): void;
}>();
const selected = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

// TODO All of this is horrible
watch(selected, (oldValue, newValue) => {
  const removedVersions = [...newValue.filter((x) => !oldValue.includes(x))];
  const addedVersions = [...oldValue.filter((x) => !newValue.includes(x))];
  for (const version of removedVersions) {
    if (!version.endsWith(".x")) {
      continue;
    }

    const parentVersion = version.substring(0, version.length - 2);
    const platformVersion = props.versions.find((v) => v.version === parentVersion);
    if (!platformVersion) {
      continue;
    }

    for (const subVersion of platformVersion.subVersions) {
      selected.value.splice(selected.value.indexOf(subVersion), 1);
    }
  }

  for (const version of addedVersions) {
    if (!version.endsWith(".x")) {
      continue;
    }

    const parentVersion = version.substring(0, version.length - 2);
    const platformVersion = props.versions.find((v) => v.version === parentVersion);
    if (!platformVersion) {
      continue;
    }

    for (const subVersion of platformVersion.subVersions) {
      if (!selected.value.includes(subVersion)) {
        selected.value.push(subVersion);
      }
    }
  }
});
</script>

<template>
  <div v-for="version in versions" :key="version.version">
    <div v-if="version.subVersions.length !== 0">
      <ArrowSpoiler :open="open">
        <template #title>
          <div class="mr-8">
            <InputCheckbox v-model="selected" :value="version.version + '.x'" :label="version.version" />
          </div>
        </template>
        <template #content>
          <div class="ml-5">
            <InputCheckbox v-for="subversion in version.subVersions" :key="subversion" v-model="selected" :value="subversion" :label="subversion" />
          </div>
        </template>
      </ArrowSpoiler>
    </div>
    <InputCheckbox v-else v-model="selected" :value="version.version" :label="version.version" />
  </div>
</template>
