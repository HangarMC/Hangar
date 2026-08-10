<script lang="ts" setup>
import type { ValidationRule } from "@vuelidate/core";
import type { PaginatedResultProject, Project } from "#shared/types/backend";

const props = defineProps<{
  label: string;
  name?: string;
  rules?: ValidationRule<string | undefined>[];
  excludeOwner?: string;
  excludeSlug?: string;
}>();

const model = defineModel<string | undefined>();
const { t } = useI18n();

const anchor = useTemplateRef<HTMLElement>("anchor");
const results = ref<Project[]>([]);
const open = ref(false);
const activeIndex = ref(-1);
const searchedQuery = ref<string>();
// fixed, not absolute: the modal body scrolls and would clip an absolute panel
const panelStyle = ref({ left: "0px", top: "0px", width: "0px" });

const query = computed(() => model.value?.trim());
const pending = computed(() => !!query.value && searchedQuery.value !== query.value);

function reposition() {
  const rect = anchor.value?.getBoundingClientRect();
  if (!rect) return;
  panelStyle.value = { left: `${rect.left}px`, top: `${rect.bottom + 4}px`, width: `${rect.width}px` };
}

let requestId = 0;
const runSearch = useDebounceFn(async (search: string) => {
  const id = ++requestId;
  let found: Project[] = [];
  try {
    const projects = await useApi<PaginatedResultProject>(`projects?limit=10&offset=0&q=${encodeURIComponent(search.replaceAll("/", " "))}`);
    found = projects.result.filter((p) => p.namespace.owner !== props.excludeOwner || p.namespace.slug !== props.excludeSlug);
  } catch {
    found = [];
  }
  if (id !== requestId) return;
  results.value = found;
  searchedQuery.value = search;
}, 250);

function ensureSearch() {
  if (!query.value) {
    results.value = [];
    searchedQuery.value = undefined;
    return;
  }
  if (!pending.value) return;
  runSearch(query.value);
}

let selecting = false;

watch(model, () => {
  if (selecting) {
    selecting = false;
    return;
  }
  activeIndex.value = -1;
  if (query.value) {
    open.value = true;
    nextTick(reposition);
  }
  ensureSearch();
});

function select(project: Project) {
  selecting = true;
  model.value = project.namespace.slug;
  open.value = false;
  activeIndex.value = -1;
}

function move(offset: number) {
  if (!open.value) {
    open.value = true;
    nextTick(reposition);
    return;
  }
  const next = activeIndex.value + offset;
  activeIndex.value = next < 0 ? results.value.length - 1 : next % results.value.length;
}

function onEnter(event: KeyboardEvent) {
  const active = results.value[activeIndex.value];
  if (!open.value || !active) return;
  event.preventDefault();
  select(active);
}

function onFocusIn() {
  if (results.value.length > 0 || query.value) open.value = true;
  nextTick(reposition);
  ensureSearch();
}

// capture: the modal body scrolls, not the window, and its scroll events don't bubble
useEventListener(window, "scroll", reposition, { capture: true });
useEventListener(window, "resize", reposition);
</script>

<template>
  <!-- listeners sit here, not on InputText: without `inheritAttrs: false` it binds them twice -->
  <div
    ref="anchor"
    role="combobox"
    :aria-expanded="open"
    @focusin="onFocusIn"
    @focusout="open = false"
    @keydown.down.prevent="move(1)"
    @keydown.up.prevent="move(-1)"
    @keydown.enter="onEnter"
    @keydown.esc="open = false"
  >
    <InputText v-model.trim="model" :label :name :rules autocomplete="off" />

    <!-- mousedown is what steals focus, so blocking it keeps the field focused until the click lands -->
    <div
      v-if="open && (results.length > 0 || query)"
      class="fixed z-50 max-h-64 min-w-0 flex flex-col gap-0.5 overflow-y-auto overflow-x-hidden rounded-md border border-gray-300 background-default p-1 shadow-default dark:border-gray-700"
      :style="panelStyle"
      role="listbox"
      @mousedown.prevent
    >
      <button
        v-for="(project, index) in results"
        :key="project.id"
        type="button"
        role="option"
        :aria-selected="index === activeIndex"
        class="w-full flex flex-shrink-0 items-center gap-2 rounded px-2 py-1.5 text-left transition-colors"
        :class="index === activeIndex ? 'background-card' : 'hover:background-card'"
        @click="select(project)"
      >
        <UserAvatar size="xs" class="flex-shrink-0" :img-src="project.avatarUrl" :monogram-name="project.name" disable-link />
        <span class="min-w-0 flex-1">
          <span class="block truncate font-medium">{{ project.name }}</span>
          <span class="block truncate text-xs text-gray-secondary">{{ project.namespace.owner }}/{{ project.namespace.slug }}</span>
        </span>
      </button>

      <p v-if="results.length === 0" class="px-2 py-1.5 text-sm text-gray-secondary">
        {{ t(pending ? "version.deps.searching" : "version.deps.noProjects") }}
      </p>
    </div>
  </div>
</template>
