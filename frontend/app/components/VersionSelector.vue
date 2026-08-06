<script lang="ts" setup>
import type { ValidationRule } from "@vuelidate/core";
import type { PlatformVersion } from "#shared/types/backend";

const props = defineProps<{
  versions: PlatformVersion[];
  modelValue?: string[];
  open: boolean;
  rules?: ValidationRule<string | undefined>[];
  col?: boolean;
  compact?: boolean;
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

// --- presentation only; selection semantics above are untouched ---

const openVersions = ref<string[]>(props.open ? props.versions.map((v) => v.version) : []);

function isOpen(version: string): boolean {
  return openVersions.value.includes(version);
}

function toggleOpen(version: string) {
  openVersions.value = isOpen(version) ? openVersions.value.filter((v) => v !== version) : [...openVersions.value, version];
}

function selectedSubCount(version: PlatformVersion): number {
  return version.subVersions.filter((s) => selectedSub.value.includes(s)).length;
}

function checkState(version: PlatformVersion): "none" | "some" | "all" {
  if (!version.subVersions?.length) return selectedSub.value.includes(version.version) ? "all" : "none";
  const count = selectedSubCount(version);
  if (count === 0) return "none";
  return count === version.subVersions.length ? "all" : "some";
}

// Reassign rather than mutate: the watchers above diff old/new, which needs a fresh array.
function toggleParent(version: PlatformVersion) {
  if (!version.subVersions?.length) {
    toggleSub(version.version);
    return;
  }
  selectedParents.value = selectedParents.value.includes(version.version)
    ? selectedParents.value.filter((v) => v !== version.version)
    : [...selectedParents.value, version.version];
}

function toggleSub(subVersion: string) {
  selectedSub.value = selectedSub.value.includes(subVersion) ? selectedSub.value.filter((v) => v !== subVersion) : [...selectedSub.value, subVersion];
}
</script>

<template>
  <InputGroup v-model="selected" :rules="rules" :silent-errors="false" full-width>
    <div :class="col || compact ? 'flex flex-col gap-0.5' : 'grid grid-cols-[repeat(auto-fill,minmax(15rem,1fr))] items-start gap-x-6 gap-y-0.5'">
      <div v-for="version in versions" :key="version.version" :class="{ 'w-full': col || compact }">
        <div class="flex items-center gap-1 rounded-md px-1 hover:background-card">
          <button
            v-if="version.subVersions?.length"
            type="button"
            class="h-5 w-5 flex flex-shrink-0 items-center justify-center rounded text-gray-secondary hover:color-primary"
            :aria-expanded="isOpen(version.version)"
            :aria-label="version.version"
            @click.prevent="toggleOpen(version.version)"
          >
            <IconMdiChevronRight class="transition-transform" :class="{ 'rotate-90': isOpen(version.version) }" />
          </button>
          <span v-else class="h-5 w-5 flex-shrink-0" />

          <label class="min-w-0 flex flex-1 cursor-pointer items-center gap-2 py-0.5 select-none">
            <span
              class="h-4 w-4 flex flex-shrink-0 items-center justify-center rounded-sm border transition-colors"
              :class="{
                'border-primary-500 bg-primary-500 text-white': checkState(version) === 'all',
                'border-primary-500 color-primary': checkState(version) === 'some',
                'border-gray-400 dark:border-gray-500': checkState(version) === 'none',
              }"
            >
              <IconMdiCheckBold v-if="checkState(version) === 'all'" class="h-3 w-3" />
              <IconMdiMinus v-else-if="checkState(version) === 'some'" class="h-3 w-3" />
            </span>
            <input type="checkbox" class="sr-only" :checked="checkState(version) === 'all'" :name="version.version" @change="toggleParent(version)" />
            <span class="truncate tabular-nums">{{ version.version }}</span>
          </label>

          <span v-if="version.subVersions?.length && selectedSubCount(version) > 0" class="flex-shrink-0 text-xs text-gray-secondary tabular-nums">
            {{ selectedSubCount(version) }}/{{ version.subVersions.length }}
          </span>
        </div>

        <div v-if="version.subVersions?.length && isOpen(version.version)" class="ml-6 mt-1 mb-1.5 flex flex-wrap gap-1">
          <button
            v-for="subversion in version.subVersions"
            :key="subversion"
            type="button"
            class="rounded px-1.5 py-0.5 text-xs font-semibold tabular-nums transition-colors"
            :class="selectedSub.includes(subversion) ? 'bg-primary-500 text-white' : 'background-card text-gray-600 dark:text-gray-300 hover:color-primary'"
            :aria-pressed="selectedSub.includes(subversion)"
            @click.prevent="toggleSub(subversion)"
          >
            {{ subversion }}
          </button>
        </div>
      </div>
    </div>
  </InputGroup>
</template>
