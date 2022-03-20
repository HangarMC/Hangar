<script lang="ts" setup>
const emit = defineEmits<{
  (e: "click"): void;
}>();
const props = withDefaults(
  defineProps<{
    small?: boolean;
  }>(),
  {
    small: false,
  }
);
</script>

<template>
  <button
    :class="'rounded-md bg-gradient-to-r from-[#004ee9] to-[#367aff] text-white h-min ' + (small ? 'p-1' : 'p-3')"
    hover="text-shadow-xl from-[#004ee9] to-[#6699ff]"
    v-bind="$attrs"
    @click="$emit('click')"
  >
    <slot></slot>
  </button>
</template>
