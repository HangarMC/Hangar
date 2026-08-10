<script lang="ts" setup>
import type { ValidationRule } from "@vuelidate/core";
import type { PlatformVersion } from "#shared/types/backend";

const props = defineProps<{
  versions: PlatformVersion[];
  modelValue?: string[];
  /**
  Force every group open or closed; omit to open only the partially selected ones.
  */
  expand?: "all" | "none";
  rules?: ValidationRule<string | undefined>[];
  col?: boolean;
  compact?: boolean;
  toolbar?: boolean;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", selected?: string[]): void;
}>();

const i18n = useI18n();

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

function leaves(version: PlatformVersion): string[] {
  return version.subVersions?.length ? version.subVersions : [version.version];
}

function selectedCount(version: PlatformVersion): number {
  return leaves(version).filter((v) => selectedSub.value.includes(v)).length;
}

function checkState(version: PlatformVersion): "none" | "some" | "all" {
  const count = selectedCount(version);
  if (count === 0) return "none";
  return count === leaves(version).length ? "all" : "some";
}

const filter = ref("");
const showFilter = computed(() => props.toolbar && props.versions.length > 8);
const filtered = computed(() => {
  const query = filter.value.trim().toLowerCase();
  if (!query) return props.versions;
  return props.versions.filter((v) => v.version.toLowerCase().includes(query) || v.subVersions?.some((s) => s.toLowerCase().includes(query)));
});

const pickedCount = computed(() => props.versions.reduce((sum, v) => sum + selectedCount(v), 0));
const pickedRange = computed(() => versionRange(props.versions.flatMap((v) => leaves(v)).filter((v) => selectedSub.value.includes(v))));

const manuallyToggled = ref(new Map<string, boolean>());
function isOpen(version: PlatformVersion): boolean {
  const override = manuallyToggled.value.get(version.version);
  if (override !== undefined) return override;
  if (filter.value.trim()) return true;
  if (props.expand) return props.expand === "all";
  return checkState(version) === "some";
}

function toggleOpen(version: PlatformVersion) {
  manuallyToggled.value.set(version.version, !isOpen(version));
}

function toggleParent(version: PlatformVersion) {
  const subs = leaves(version);
  selectedSub.value = checkState(version) === "all" ? selectedSub.value.filter((v) => !subs.includes(v)) : [...new Set([...selectedSub.value, ...subs])];
}

function toggleSub(subVersion: string) {
  selectedSub.value = selectedSub.value.includes(subVersion) ? selectedSub.value.filter((v) => v !== subVersion) : [...selectedSub.value, subVersion];
}

function clear() {
  selectedSub.value = [];
}
</script>

<template>
  <div>
    <div v-if="toolbar" class="mb-2 flex items-center gap-3">
      <div class="min-w-0 flex flex-1 items-center gap-1 text-sm">
        <span v-if="pickedCount > 0" class="truncate font-semibold tabular-nums">{{ pickedRange }}</span>
        <span v-else class="text-gray-secondary">{{ i18n.t("version.platformSelect.none") }}</span>
        <Button v-if="pickedCount > 0" variant="ghost" tone="neutral" size="sm" @click="clear">{{ i18n.t("version.platformSelect.clear") }}</Button>
      </div>
      <div v-if="showFilter" class="relative w-40 flex-shrink-0">
        <IconMdiMagnify class="pointer-events-none absolute left-2.5 top-2 text-gray-secondary" />
        <input
          v-model="filter"
          type="search"
          class="w-full rounded-md background-card py-1.5 pl-8 pr-2 text-sm outline-none focus:(ring-2 ring-primary-500)"
          :placeholder="i18n.t('version.platformSelect.filter')"
        />
      </div>
    </div>

    <InputGroup v-model="selected" :rules="rules" :silent-errors="false" full-width>
      <div :class="col || compact ? 'flex flex-col gap-1.5' : 'grid gap-x-3 gap-y-1.5 sm:grid-cols-2'">
        <div
          v-for="version in filtered"
          :key="version.version"
          class="h-max overflow-hidden rounded-md border transition-colors"
          :class="checkState(version) === 'none' ? 'border-gray-300 dark:border-gray-700' : 'border-gray-400 dark:border-gray-600'"
        >
          <div class="flex items-center" :class="{ 'background-card': checkState(version) === 'all' }">
            <button
              type="button"
              class="min-w-0 h-8 flex flex-1 items-center gap-2 pl-2 text-left transition-colors hover:text-black dark:hover:text-white"
              :aria-pressed="checkState(version) === 'all'"
              @click.prevent="toggleParent(version)"
            >
              <span
                class="h-4 w-4 flex flex-shrink-0 items-center justify-center rounded-sm border transition-colors"
                :class="checkState(version) === 'none' ? 'border-gray-400 dark:border-gray-500' : 'vs-box-on'"
              >
                <IconMdiCheckBold v-if="checkState(version) === 'all'" class="text-[10px]" />
                <IconMdiMinus v-else-if="checkState(version) === 'some'" class="text-[10px]" />
              </span>
              <span class="truncate tabular-nums" :class="{ 'font-semibold': checkState(version) !== 'none' }">{{ version.version }}</span>
            </button>

            <span
              v-if="version.subVersions?.length"
              class="flex-shrink-0 text-xs tabular-nums"
              :class="selectedCount(version) > 0 ? '' : 'text-gray-secondary'"
            >
              {{ selectedCount(version) }}/{{ version.subVersions.length }}
            </span>
            <button
              v-if="version.subVersions?.length"
              type="button"
              class="h-8 w-7 flex flex-shrink-0 items-center justify-center text-gray-secondary hover:text-black dark:hover:text-white"
              :aria-expanded="isOpen(version)"
              :aria-label="version.version"
              @click.prevent="toggleOpen(version)"
            >
              <IconMdiChevronRight class="transition-transform" :class="{ 'rotate-90': isOpen(version) }" />
            </button>
            <span v-else class="w-2 flex-shrink-0" />
          </div>

          <div v-if="version.subVersions?.length && isOpen(version)" class="flex flex-wrap gap-1 border-t border-gray-300 px-1.5 py-1.5 dark:border-gray-700">
            <button
              v-for="subversion in version.subVersions"
              :key="subversion"
              type="button"
              class="rounded border px-1.5 py-0.5 text-xs tabular-nums transition-colors"
              :class="
                selectedSub.includes(subversion)
                  ? 'border-gray-400 background-card font-semibold dark:border-gray-600'
                  : 'border-transparent text-gray-600 hover:background-card dark:text-gray-300'
              "
              :aria-pressed="selectedSub.includes(subversion)"
              @click.prevent="toggleSub(subversion)"
            >
              {{ subversion }}
            </button>
          </div>
        </div>
      </div>
      <p v-if="filtered.length === 0" class="py-3 text-sm text-gray-secondary">{{ i18n.t("version.platformSelect.noMatches", [filter]) }}</p>
    </InputGroup>
  </div>
</template>

<style scoped>
.vs-box-on {
  border-color: var(--gray-500);
  background-color: var(--gray-500);
  color: var(--gray-50);
}

.dark .vs-box-on {
  border-color: var(--gray-400);
  background-color: var(--gray-400);
  color: var(--gray-900);
}
</style>
