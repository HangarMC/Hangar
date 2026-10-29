<script lang="ts" setup generic="T extends string">
import type { VNode } from "vue";
import type { Tab } from "#shared/types/components/design/Tabs";

const route = useRoute();

const emit = defineEmits<{
  (e: "update:modelValue", value?: string): void;
}>();

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    tabs: Tab<T>[];
    vertical?: boolean;
    compact?: boolean;
    router?: boolean;
    highlightSelected?: boolean;
    divided?: boolean;
  }>(),
  {
    modelValue: undefined,
    vertical: true,
    compact: false,
    router: false,
    highlightSelected: false,
    divided: false,
  }
);

const internalValue = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

watch(internalValue, (n) => {
  if (props.tabs.length > 0 && props.tabs.every((t) => t.value !== n)) {
    internalValue.value = props.tabs[0]!.value;
  }
});

function selectTab(event: Event, tab: Tab<T>) {
  if (!props.router) {
    event.preventDefault();
  }
  if (!tab.disable || !tab.disable()) {
    internalValue.value = tab.value;
  }
}

function isSelected(tab: Tab<T>) {
  return (props.router ? route.path.slice(Math.max(0, route.path.lastIndexOf("/") + 1)) : internalValue.value) === tab.value;
}

defineSlots<
  {
    [A in T]: () => VNode;
  } & {
    catchall: () => VNode;
    default: () => VNode;
  }
>();
</script>

<template>
  <div :class="{ 'flex flex-col lt-md:space-y-2 md:flex-row': vertical, 'md:space-x-2': !compact && vertical, 'flex flex-row flex-wrap': !vertical }">
    <div
      :class="{
        'min-w-13ch': vertical,
        'basis-full': !vertical,
        'lt-md:(border-b pb-2) md:(border-r pr-3) border-gray-300 dark:border-gray-700': divided && vertical,
      }"
    >
      <ul :class="{ 'flex flex-row flex-wrap lt-md:gap-1 md:flex-col': vertical, 'md:space-y-1': !compact && vertical, 'flex flex-row gap-1': !vertical }">
        <li v-for="tab in tabs" :key="tab.value" :class="{ 'md:(mt-2 border-t border-gray-300 pt-2) dark:md:border-gray-700': tab.separated && vertical }">
          <Button
            v-if="!tab.show || tab.show()"
            variant="ghost"
            tone="neutral"
            :disabled="tab.disable && tab.disable()"
            :href="router ? undefined : '#' + tab.value"
            :to="router ? tab.value : undefined"
            :aria-current="isSelected(tab) ? 'page' : undefined"
            :class="{
              'tab-selected': isSelected(tab),
              'md:w-full md:!justify-start': vertical && highlightSelected,
            }"
            @click="selectTab($event, tab)"
          >
            <component :is="tab.icon" v-if="tab.icon" />
            {{ tab.header }}
          </Button>
        </li>
      </ul>
      <hr v-if="!vertical" class="mb-2" />
    </div>

    <div class="min-w-0 flex-grow" :class="{ 'md:pl-4': divided && vertical }">
      <template v-if="router">
        <slot v-if="router" />
      </template>
      <template v-else>
        <template v-for="tab in tabs" :key="tab.value">
          <slot v-if="internalValue === tab.value" :name="tab.value" />
        </template>
        <slot name="catchall" />
      </template>
    </div>
  </div>
</template>

<style scoped>
.tab-selected.tab-selected {
  background-color: color-mix(in srgb, var(--primary-500) 7%, #ffffff);
  color: var(--primary-ink);
}

.dark .tab-selected.tab-selected {
  background-color: color-mix(in srgb, var(--primary-500) 13%, var(--gray-800));
  color: #f4f4f5;
}
</style>
