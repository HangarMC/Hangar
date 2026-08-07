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

const selectedSub = ref<string[]>(selected.value ? [...new Set(selected.value)] : []);

watch(selectedSub, (value) => {
  const result = new Set(value);
  for (const version of props.versions) {
    if (version.subVersions.length > 0 && version.subVersions.every((v) => result.has(v))) {
      result.add(version.version);
    }
  }
  selected.value = [...result];
});

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

function toggleParent(version: PlatformVersion) {
  const subs = version.subVersions?.length ? version.subVersions : [version.version];
  selectedSub.value = checkState(version) === "all" ? selectedSub.value.filter((v) => !subs.includes(v)) : [...new Set([...selectedSub.value, ...subs])];
}

function toggleSub(subVersion: string) {
  selectedSub.value = selectedSub.value.includes(subVersion) ? selectedSub.value.filter((v) => v !== subVersion) : [...selectedSub.value, subVersion];
}
</script>

<template>
  <InputGroup v-model="selected" :rules="rules" :silent-errors="false" full-width>
    <div :class="col || compact ? '' : 'gap-x-3 sm:columns-2'">
      <div
        v-for="version in versions"
        :key="version.version"
        class="mb-1.5 break-inside-avoid overflow-hidden rounded-md border transition-colors"
        :class="checkState(version) === 'none' ? 'border-gray-300 dark:border-gray-700' : 'vs-panel-active'"
      >
        <div class="flex items-center" :class="{ 'vs-head-all': checkState(version) === 'all' }">
          <button
            v-if="version.subVersions?.length"
            type="button"
            class="h-8 w-7 flex flex-shrink-0 items-center justify-center text-gray-secondary hover:color-primary"
            :aria-expanded="isOpen(version.version)"
            :aria-label="version.version"
            @click.prevent="toggleOpen(version.version)"
          >
            <IconMdiChevronRight class="transition-transform" :class="{ 'rotate-90': isOpen(version.version) }" />
          </button>
          <span v-else class="w-2.5 flex-shrink-0" />

          <button
            type="button"
            class="min-w-0 h-8 flex flex-1 items-center gap-2 pr-2 text-left transition-colors hover:color-primary"
            :aria-pressed="checkState(version) === 'all'"
            @click.prevent="toggleParent(version)"
          >
            <span class="truncate tabular-nums" :class="{ 'font-semibold': checkState(version) !== 'none' }">{{ version.version }}</span>
            <span
              v-if="version.subVersions?.length"
              class="ml-auto flex-shrink-0 text-xs tabular-nums"
              :class="selectedSubCount(version) > 0 ? 'font-semibold color-primary' : 'text-gray-secondary'"
            >
              {{ selectedSubCount(version) }}/{{ version.subVersions.length }}
            </span>
          </button>
        </div>

        <div
          v-if="version.subVersions?.length && isOpen(version.version)"
          class="flex flex-wrap gap-1 border-t border-gray-300 px-1.5 py-1.5 dark:border-gray-700"
        >
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

<style scoped>
.vs-panel-active {
  border-color: color-mix(in srgb, var(--primary-500) 55%, transparent);
}

.vs-head-all {
  background-color: color-mix(in srgb, var(--primary-500) 12%, transparent);
}
</style>
