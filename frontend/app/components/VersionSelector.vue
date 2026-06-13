<script lang="ts" setup>
import type { ValidationRule } from "@vuelidate/core";
import type { PlatformVersion } from "#shared/types/backend";

const props = defineProps<{
  versions: PlatformVersion[];
  modelValue?: string[];
  open: boolean;
  showAllVersions: boolean;
  versionSearchQuery?: string;
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

const missingPatchVersions: Record<string, string[]> = {
  "1.21": ["1.21.11", "1.21.10"],
};

function patchParent(version: string): string | undefined {
  const segments = version.split(".");
  if (segments.length !== 3 || segments.some((segment) => !/^\d+$/.test(segment)) || Number(segments[0]) <= 1) {
    return undefined;
  }
  return segments.slice(0, 2).join(".");
}

const normalizedVersions = computed<PlatformVersion[]>(() => {
  const groupedVersions = new Map<string, { subVersions: string[]; hasSubVersions: boolean }>();

  for (const platformVersion of props.versions) {
    const parent = platformVersion.subVersions.length === 0 ? patchParent(platformVersion.version) : undefined;
    const group = parent ?? platformVersion.version;
    const existingGroup = groupedVersions.get(group) ?? { subVersions: [], hasSubVersions: false };
    const subVersions = platformVersion.subVersions.length > 0 ? platformVersion.subVersions : [platformVersion.version];

    for (const subVersion of subVersions) {
      if (!existingGroup.subVersions.includes(subVersion)) {
        existingGroup.subVersions.push(subVersion);
      }
    }
    existingGroup.hasSubVersions ||= platformVersion.subVersions.length > 0 || parent !== undefined;
    groupedVersions.set(group, existingGroup);
  }

  for (const [parent, missingVersions] of Object.entries(missingPatchVersions)) {
    const group = groupedVersions.get(parent);
    if (!group) {
      continue;
    }
    group.subVersions = [...missingVersions, ...group.subVersions.filter((version) => !missingVersions.includes(version))];
    group.hasSubVersions = true;
  }

  return [...groupedVersions].map(([version, group]) => ({
    version,
    subVersions: group.hasSubVersions ? group.subVersions : [],
  }));
});

const realVersions = computed(
  () => new Set(normalizedVersions.value.flatMap((version) => (version.subVersions.length > 0 ? version.subVersions : [version.version])))
);

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
    const platformVersion = normalizedVersions.value.find((v) => v.version === cutVersion || v.version === version);
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
    if (!newValue?.length) {
      selectedParents.value = [];
      selectedSub.value = [];
    }
  }
);
const filteredVersions = computed(() => {
  const versionSearchQuery = props.versionSearchQuery?.toLowerCase() ?? "";
  return normalizedVersions.value.filter((version) => version.version.toLowerCase().includes(versionSearchQuery));
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
    const platformVersion = normalizedVersions.value.find((v) => v.version === version);
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
    const platformVersion = normalizedVersions.value.find((v) => v.version === version);
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
    const platformVersion = normalizedVersions.value.find((v) => v.version === cutVersion || v.version === version);
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
    const platformVersion = normalizedVersions.value.find((v) => v.version === cutVersion || v.version === version);
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
      if (realVersions.value.has(platformVersion.version) && !selected.value.includes(platformVersion.version)) {
        selected.value.push(platformVersion.version);
      }
    }
  }
}

const i18n = useI18n();
</script>

<template>
  <InputGroup v-model="selected" :rules="rules" :silent-errors="false" full-width>
    <div class="flex flex-col gap-1">
      <template v-if="filteredVersions.length === 0">
        <span class="px-3 py-8 text-center text-sm text-gray">{{ i18n.t("hangar.projectSearch.noVersions") }}</span>
      </template>
      <template v-for="version in filteredVersions" v-else :key="version.version">
        <template v-if="version.subVersions?.length !== 0">
          <template v-if="!showAllVersions">
            <div class="rounded-lg transition-colors hover:bg-gray-100 dark:hover:bg-gray-800">
              <InputCheckbox v-model="selectedParents" :value="version.version" :label="version.version" :name="version.version" />
            </div>
          </template>
          <template v-for="subversion in version.subVersions" v-else :key="subversion">
            <div class="rounded-lg transition-colors hover:bg-gray-100 dark:hover:bg-gray-800">
              <InputCheckbox v-model="selectedSub" :value="subversion" :label="subversion" :name="subversion" />
            </div>
          </template>
        </template>
        <template v-else>
          <div class="rounded-lg transition-colors hover:bg-gray-100 dark:hover:bg-gray-800">
            <InputCheckbox v-model="selectedSub" :value="version.version" :label="version.version" :name="version.version" />
          </div>
        </template>
      </template>
    </div>
  </InputGroup>
</template>
