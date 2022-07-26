<script lang="ts" setup>
import { computed } from "vue";
import Button from "~/lib/components/design/Button.vue";
import Link from "~/lib/components/design/Link.vue";

const emit = defineEmits<{
  (e: "update:modelValue", value: string): void;
}>();
const internalValue = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

export interface Tab {
  value: string;
  header: string;
  show?: () => boolean;
  disable?: () => boolean;
}

const props = withDefaults(
  defineProps<{
    modelValue: string;
    tabs: Tab[];
    vertical?: boolean;
    compact?: boolean;
  }>(),
  {
    vertical: true,
    compact: false,
  }
);

function selectTab(tab: Tab) {
  if (!tab.disable || !tab.disable()) {
    internalValue.value = tab.value;
  }
}
</script>

<template>
  <div :class="{ 'flex flex-col <md:space-y-2 md:flex-row': vertical, 'md:space-x-2': !compact && vertical, 'flex flex-row flex-wrap': !vertical }">
    <div :class="{ 'min-w-13ch': vertical, 'basis-full': !vertical }">
      <ul :class="{ 'flex flex-row flex-wrap <md:gap-2 md:flex-col': vertical, 'md:space-y-2': !compact && vertical, 'flex flex-row gap-1': !vertical }">
        <li v-for="tab in tabs" :key="tab.value">
          <Link v-if="!tab.show || tab.show()" :disabled="tab.disable && tab.disable()" :href="'#' + tab.value" @click.prevent="selectTab(tab)">
            <Button
              v-if="!tab.show || tab.show()"
              :disabled="tab.disable && tab.disable()"
              :class="internalValue === tab.value ? 'underline' : '!font-semibold'"
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
      <template v-for="tab in tabs" :key="tab.value">
        <slot v-if="internalValue === tab.value" :name="tab.value" />
      </template>
      <slot name="catchall" />
    </div>
  </div>
</template>
