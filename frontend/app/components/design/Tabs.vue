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
  }>(),
  {
    modelValue: undefined,
    vertical: true,
    compact: false,
    router: false,
  }
);

const internalValue = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

watch(internalValue, (n) => {
  if (props.tabs.length > 0 && !props.tabs.some((t) => t.value === n)) {
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
    <div :class="{ 'min-w-13ch': vertical, 'basis-full': !vertical }">
      <ul :class="{ 'flex flex-row flex-wrap lt-md:gap-1 md:flex-col': vertical, 'md:space-y-1': !compact && vertical, 'flex flex-row gap-1': !vertical }">
        <li v-for="tab in tabs" :key="tab.value">
          <Link
            v-if="!tab.show || tab.show()"
            :disabled="tab.disable && tab.disable()"
            :href="router ? undefined : '#' + tab.value"
            :to="router ? tab.value : undefined"
            @click="selectTab($event, tab)"
          >
            <Button
              v-if="!tab.show || tab.show()"
              :disabled="tab.disable && tab.disable()"
              :class="
                (router ? route.path.substring(route.path.lastIndexOf('/') + 1) == tab.value : internalValue === tab.value) ? 'underline' : '!font-semibold'
              "
              size="medium"
              button-type="transparent"
            >
              {{ tab.header }}
            </Button>
          </Link>
        </li>
      </ul>
      <hr v-if="!vertical" class="mb-2" />
    </div>
    <div class="flex-grow">
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
