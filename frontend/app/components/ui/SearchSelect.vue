<script lang="ts" setup generic="T">
import type { ValidationRule } from "@vuelidate/core";

const props = defineProps<{
  label: string;
  name?: string;
  rules?: ValidationRule<string | undefined>[];
  errorMessages?: string[];
  search: (query: string) => Promise<T[]>;
  optionKey: (item: T) => string | number;
  optionValue: (item: T) => string;
  emptyMessage: string;
}>();

const model = defineModel<string | undefined>();
const { t } = useI18n();

const anchor = useTemplateRef<HTMLElement>("anchor");
const results = shallowRef<T[]>([]);
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
  let found: T[] = [];
  try {
    found = await props.search(search);
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

function select(item: T) {
  selecting = true;
  model.value = props.optionValue(item);
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
    <InputText v-model.trim="model" :label :name :rules :error-messages autocomplete="off" />

    <!-- mousedown is what steals focus, so blocking it keeps the field focused until the click lands -->
    <div
      v-if="open && (results.length > 0 || query)"
      class="fixed z-50 max-h-64 min-w-0 flex flex-col gap-0.5 overflow-y-auto overflow-x-hidden rounded-md border border-gray-300 background-default p-1 shadow-default dark:border-gray-700"
      :style="panelStyle"
      role="listbox"
      @mousedown.prevent
    >
      <button
        v-for="(item, index) in results"
        :key="optionKey(item)"
        type="button"
        role="option"
        :aria-selected="index === activeIndex"
        class="w-full flex flex-shrink-0 items-center gap-2 rounded px-2 py-1.5 text-left transition-colors"
        :class="index === activeIndex ? 'background-card' : 'hover:background-card'"
        @click="select(item)"
      >
        <slot name="option" :item="item" />
      </button>

      <p v-if="results.length === 0" class="px-2 py-1.5 text-sm text-gray-secondary">
        {{ pending ? t("general.searching") : emptyMessage }}
      </p>
    </div>
  </div>
</template>
