<script lang="ts" setup>
import type { Ref } from "vue";
import { computed, watch } from "vue";
import type { PlatformVersion } from "hangar-internal";
import type { ValidationRule } from "@vuelidate/core";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import ArrowSpoiler from "~/components/design/ArrowSpoiler.vue";
import { ref } from "#imports";
import InputGroup from "~/components/ui/InputGroup.vue";

const props = defineProps<{
  versions: PlatformVersion[];
  modelValue: string[];
  open: boolean;
  rules?: ValidationRule<string | undefined>[];
  col?: boolean;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", selected: string[]): void;
}>();
const selected = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

const selectedParents: Ref<string[]> = ref([]);
const selectedSub: Ref<string[]> = ref([]);
for (const version of selected.value) {
  selectedSub.value.push(version);

  const lastSeparator = version.lastIndexOf(".");
  if (lastSeparator === -1) {
    continue;
  }
  const cutVersion = version.substring(0, lastSeparator);
  const platformVersion = props.versions.find((v) => v.version === cutVersion || v.version === version);
  if (!platformVersion) {
    continue;
  }
  let selectedAll = true;
  for (const v of platformVersion.subVersions) {
    if (!selectedSub.value.includes(v)) {
      selectedAll = false;
      break;
    }
  }
  if (selectedAll) {
    selectedParents.value.push(platformVersion.version);
  }
}

// TODO All of this is horrible
watch(selectedParents, (oldValue, newValue) => {
  handleRemovedParent([...newValue.filter((x) => !oldValue.includes(x))]);
  handleAddedParent([...oldValue.filter((x) => !newValue.includes(x))]);
});
watch(selectedSub, (oldValue, newValue) => {
  handleRemovedSub([...newValue.filter((x) => !oldValue.includes(x))]);
  handleAddedSub([...oldValue.filter((x) => !newValue.includes(x))]);
});

function handleRemovedParent(removedVersions: string[]) {
  for (const version of removedVersions) {
    const platformVersion = props.versions.find((v) => v.version === version);
    if (!platformVersion) {
      continue;
    }

    // Remove all sub versions
    for (const subVersion of platformVersion.subVersions) {
      selected.value.splice(selected.value.indexOf(subVersion), 1);
      selectedSub.value.splice(selectedSub.value.indexOf(subVersion), 1);
    }
  }
}

function handleAddedParent(addedVersions: string[]) {
  for (const version of addedVersions) {
    const platformVersion = props.versions.find((v) => v.version === version);
    if (!platformVersion) {
      continue;
    }

    // Add all sub versions
    for (const subVersion of platformVersion.subVersions) {
      selected.value.push(subVersion);
      selectedSub.value.push(subVersion);
    }
  }
}

function handleRemovedSub(removedVersions: string[]) {
  for (const version of removedVersions) {
    if (selected.value.includes(version)) {
      selected.value.splice(selected.value.indexOf(version), 1);
    }

    const lastSeparator = version.lastIndexOf(".");
    if (lastSeparator === -1) {
      continue;
    }

    const cutVersion = version.substring(0, lastSeparator);
    const platformVersion = props.versions.find((v) => v.version === cutVersion || v.version === version);
    if (!platformVersion) {
      continue;
    }

    // Unselect parent
    if (selectedParents.value.includes(platformVersion.version)) {
      selectedParents.value.splice(selectedParents.value.indexOf(platformVersion.version), 1);
    }
  }
}

function handleAddedSub(removedVersions: string[]) {
  for (const version of removedVersions) {
    if (!selected.value.includes(version)) {
      selected.value.push(version);
    }

    const lastSeparator = version.lastIndexOf(".");
    if (lastSeparator === -1) {
      continue;
    }

    const cutVersion = version.substring(0, lastSeparator);
    const platformVersion = props.versions.find((v) => v.version === cutVersion || v.version === version);
    if (!platformVersion) {
      continue;
    }

    // Select parent if all subversions are selected
    let selectedAll = true;
    for (const v of platformVersion.subVersions) {
      if (!selectedSub.value.includes(v)) {
        selectedAll = false;
        break;
      }
    }

    if (selectedAll) {
      if (!selectedParents.value.includes(platformVersion.version)) {
        selectedParents.value.push(platformVersion.version);
      }
      if (!selected.value.includes(platformVersion.version)) {
        selected.value.push(platformVersion.version);
      }
    }
  }
}
</script>

<template>
  <InputGroup v-model="selected" :rules="rules" :silent-errors="false" full-width>
    <div :class="{ 'flex items-start flex-wrap': true, 'flex-col': col, 'gap-4': !col }">
      <div v-for="version in versions" :key="version.version">
        <template v-if="version.subVersions?.length !== 0">
          <ArrowSpoiler :open="open">
            <template #title>
              <div class="mr-7">
                <InputCheckbox v-model="selectedParents" :value="version.version" :label="version.version" />
              </div>
            </template>
            <template #content>
              <div class="ml-4">
                <InputCheckbox v-for="subversion in version.subVersions" :key="subversion" v-model="selectedSub" :value="subversion" :label="subversion" />
              </div>
            </template>
          </ArrowSpoiler>
        </template>
        <InputCheckbox v-else v-model="selectedSub" :value="version.version" :label="version.version" />
      </div>
    </div>
  </InputGroup>
</template>
