<script lang="ts" setup>
const emit = defineEmits<{
  (e: "click"): void;
}>();
const props = withDefaults(
  defineProps<{
    small?: boolean;
    disabled?: boolean;
  }>(),
  {
    small: false,
    disabled: false,
  }
);
</script>

<template>
  <button
    :class="'rounded-md text-white h-min ' + (small ? 'p-1' : 'p-3') + (disabled ? ' bg-gray-500' : ' bg-gradient-to-r from-[#004ee9] to-[#367aff]')"
    :hover="disabled ? '' : 'text-shadow-xl from-[#004ee9] to-[#6699ff]'"
    :disabled="disabled"
    v-bind="$attrs"
    @click="$emit('click')"
  >
    <slot></slot>
  </button>
</template>
