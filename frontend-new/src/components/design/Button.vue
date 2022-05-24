<script lang="ts" setup>
import { computed } from "vue";
import Spinner from "~/components/design/Spinner.vue";
import { RouteLocationRaw } from "vue-router";

const emit = defineEmits<{
  (e: "click"): void;
}>();
const props = withDefaults(
  defineProps<{
    disabled?: boolean;
    size?: "small" | "medium" | "large";
    buttonType?: "primary" | "secondary" | "red" | "transparent";
    loading?: boolean;
    to?: string | RouteLocationRaw | object;
  }>(),
  {
    disabled: false,
    size: "small",
    buttonType: "primary",
    loading: false,
    to: undefined,
  }
);
const paddingClass = computed(() => {
  switch (props.size) {
    default:
    case "small": {
      return "p-1 px-2 space-x-0.7";
    }
    case "medium": {
      return "p-2 space-x-0.7";
    }
    case "large": {
      return "p-3 space-x-1.2";
    }
  }
});
</script>

<template>
  <component
    :is="to ? 'router-link' : 'button'"
    :class="
      'rounded-md font-semibold h-min inline-flex items-center justify-center text-white disabled:(bg-gray-300 cursor-not-allowed) disabled:dark:(text-gray-500 bg-gray-700) ' +
      paddingClass +
      ' button-' +
      buttonType +
      (loading ? ' !cursor-wait' : '')
    "
    :disabled="disabled || loading"
    :to="to"
    v-bind="$attrs"
    @click="$emit('click')"
  >
    <slot></slot>
    <span v-if="loading" class="pl-1"><Spinner class="stroke-gray-400" :diameter="1" :stroke="0.01" unit="rem" /></span>
  </component>
</template>
