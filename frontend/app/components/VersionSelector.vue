<script lang="ts" setup>
import type { ValidationRule } from "@vuelidate/core";
import type { PlatformVersion } from "#shared/types/backend";

const props = defineProps<{
  versions: PlatformVersion[];
  modelValue?: string[];
  open: boolean;
  showAllVersions: boolean;
  versionSearchQuery: string;
  rules?: ValidationRule<string | undefined>[];
  col?: boolean;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", selected?: string[]): void;
}>();
const selected = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

const selectedParents = ref<string[]>([]);
const selectedSub = ref<string[]>([]);
if (selected.value) {
  for (const version of selected.value) {
    selectedSub.value.push(version);

    const lastSeparator = version.lastIndexOf(".");
    if (lastSeparator === -1) {
      continue;
    }
    const cutVersion = version.slice(0, Math.max(0, lastSeparator));
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
}

// TODO All of this is horrible
watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue.length === 0) {
      selectedParents.value = [];
      selectedSub.value = [];
    }
  }
);
const filteredVersions = computed(() => {
  return props.versions.filter((version) =>
    version.version.toLowerCase().includes(props.versionSearchQuery.toLowerCase())
  );
});

watch(selectedParents, (oldValue, newValue) => {
  handleRemovedParent(newValue.filter((x) => !oldValue.includes(x)));
  handleAddedParent(oldValue.filter((x) => !newValue.includes(x)));
});
watch(selectedSub, (oldValue, newValue) => {
  handleRemovedSub(newValue.filter((x) => !oldValue.includes(x)));
  handleAddedSub(oldValue.filter((x) => !newValue.includes(x)));
});

function handleRemovedParent(removedVersions: string[]) {
  for (const version of removedVersions) {
    const platformVersion = props.versions.find((v) => v.version === version);
    if (!platformVersion) {
      continue;
    }

    // Remove all sub versions
    for (const subVersion of platformVersion.subVersions) {
      selected.value?.splice(selected.value.indexOf(subVersion), 1);
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
      selected.value?.push(subVersion);
      selectedSub.value.push(subVersion);
    }
  }
}

function handleRemovedSub(removedVersions: string[]) {
  for (const version of removedVersions) {
    if (selected.value?.includes(version)) {
      selected.value.splice(selected.value.indexOf(version), 1);
    }

    const lastSeparator = version.lastIndexOf(".");
    if (lastSeparator === -1) {
      continue;
    }

    const cutVersion = version.slice(0, Math.max(0, lastSeparator));
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
  if (!selected.value) return;
  for (const version of removedVersions) {
    if (!selected.value.includes(version)) {
      selected.value.push(version);
    }

    const lastSeparator = version.lastIndexOf(".");
    if (lastSeparator === -1) {
      continue;
    }

    const cutVersion = version.slice(0, Math.max(0, lastSeparator));
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

const i18n = useI18n();
</script>

<template>
  <InputGroup v-model="selected" :rules="rules" :silent-errors="false" full-width>
    <div class="flex flex-col gap-1 mt-2">
      <template v-if="filteredVersions.length === 0">
        <span class="text-gray-400 mt-16">{{ i18n.t("hangar.projectSearch.noVersions") }}</span>
      </template>
      <template v-for="version in filteredVersions" v-else :key="version.version">
        <template v-if="version.subVersions?.length !== 0">
          <template v-if="!showAllVersions">
            <div class="mr-4 ml-1">
              <InputCheckbox v-model="selectedParents" :value="version.version" :label="version.version" :name="version.version" />
            </div>
          </template>
          <template v-for="subversion in version.subVersions" v-else :key="subversion" >
            <div class="mr-4 ml-1">
              <InputCheckbox
                v-model="selectedSub"
                :value="subversion"
                :label="subversion"
                :name="subversion"
              />
            </div>
          </template>
        </template>
        <template v-else>
          <div class="mr-4 ml-1">
            <InputCheckbox v-model="selectedSub" :value="version.version" :label="version.version" :name="version.version" />
          </div>
        </template>
      </template>
    </div>
  </InputGroup>
</template>
