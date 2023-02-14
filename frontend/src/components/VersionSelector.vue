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
  handleRemoved([...newValue.filter((x) => !oldValue.includes(x))]);
  handleAdded([...oldValue.filter((x) => !newValue.includes(x))]);
});

function handleRemoved(removedVersions: string[]) {
  for (const version of removedVersions) {
    if (!version.endsWith(".x")) {
      handleRemovedSubVersion(version);
      continue;
    }

    const parentVersion = version.substring(0, version.length - 2);
    const platformVersion = props.versions.find((v) => v.version === parentVersion);
    if (!platformVersion) {
      continue;
    }

    // Remove all sub versions
    for (const subVersion of platformVersion.subVersions) {
      selected.value.splice(selected.value.indexOf(subVersion), 1);
    }
  }
}

function handleAdded(addedVersions: string[]) {
  for (const version of addedVersions) {
    if (!version.endsWith(".x")) {
      handleAddedSubVersion(version);
      continue;
    }

    const parentVersion = version.substring(0, version.length - 2);
    const platformVersion = props.versions.find((v) => v.version === parentVersion);
    if (!platformVersion) {
      continue;
    }

    // Add all sub versions
    for (const subVersion of platformVersion.subVersions) {
      if (!selected.value.includes(subVersion)) {
        selected.value.push(subVersion);
      }
    }
  }
}

function handleRemovedSubVersion(version: string) {
  const lastSeparator = version.lastIndexOf(".");
  if (lastSeparator === -1) {
    return;
  }

  const cutVersion = version.substring(0, lastSeparator);
  const platformVersion = props.versions.find((v) => v.version === cutVersion || v.version === version);
  if (!platformVersion || !selected.value.includes(platformVersion.version + ".x")) {
    return;
  }

  for (const subVersion of platformVersion.subVersions) {
    if (selected.value.includes(subVersion)) {
      // Uncheck parent box
      selected.value.splice(selected.value.indexOf(platformVersion.version + ".x"), 1);
      return;
    }
  }
}

function handleAddedSubVersion(version: string) {
  const lastSeparator = version.lastIndexOf(".");
  if (lastSeparator === -1) {
    return;
  }

  const cutVersion = version.substring(0, lastSeparator);
  const platformVersion = props.versions.find((v) => v.version === cutVersion || v.version === version);
  if (!platformVersion || selected.value.includes(platformVersion.version + ".x")) {
    return;
  }

  for (const subVersion of platformVersion.subVersions) {
    if (!selected.value.includes(subVersion)) {
      return;
    }
  }

  // Check parent box
  selected.value.push(platformVersion.version + ".x");
}
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
