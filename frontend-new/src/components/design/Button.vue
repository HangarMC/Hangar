<script lang="ts" setup>
import { computed } from "vue";

const emit = defineEmits<{
  (e: "click"): void;
}>();
const props = withDefaults(
  defineProps<{
    disabled?: boolean;
    size?: "small" | "medium" | "large";
  }>(),
  {
    disabled: false,
    size: "small",
  }
);
const paddingClass = computed(() => {
  switch (props.size) {
    default:
    case "small": {
      return "p-1";
    }
    case "medium": {
      return "p-2";
    }
    case "large": {
      return "p-3";
    }
  }
});
</script>

<template>
  <button
    :class="'rounded-md text-white h-min ' + paddingClass + (disabled ? ' bg-gray-500' : ' bg-gradient-to-r from-[#004ee9] to-[#367aff]')"
    :hover="disabled ? '' : 'text-shadow-xl from-[#004ee9] to-[#6699ff]'"
    :disabled="disabled"
    v-bind="$attrs"
    @click="$emit('click')"
  >
    <slot></slot>
  </button>
</template>
