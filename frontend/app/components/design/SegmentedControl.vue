<script setup lang="ts" generic="T extends string">
const props = defineProps<{
  modelValue: T;
  options: { value: T; label: string; count?: number }[];
  ariaLabel?: string;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: T];
}>();

const container = useTemplateRef<HTMLElement>("container");

function move(offset: number) {
  const idx = props.options.findIndex((o) => o.value === props.modelValue);
  const next = props.options[(idx + offset + props.options.length) % props.options.length];
  if (!next) return;
  emit("update:modelValue", next.value);
  nextTick(() => container.value?.querySelector<HTMLElement>('[aria-selected="true"]')?.focus());
}
</script>

<template>
  <div
    ref="container"
    role="tablist"
    :aria-label="ariaLabel"
    class="inline-flex gap-0.5 rounded-lg background-card p-1"
    @keydown.left.prevent="move(-1)"
    @keydown.right.prevent="move(1)"
  >
    <button
      v-for="option in options"
      :key="option.value"
      type="button"
      role="tab"
      :aria-selected="modelValue === option.value"
      :tabindex="modelValue === option.value ? 0 : -1"
      class="inline-flex items-center gap-1.5 rounded-md px-3 py-1.5 text-sm font-semibold transition-colors focus-visible:(outline-2 outline-offset-2 outline-primary-500)"
      :class="
        modelValue === option.value ? 'background-default color-primary shadow-sm' : 'text-gray-600 dark:text-gray-300 hover:(text-black dark:text-white)'
      "
      @click="emit('update:modelValue', option.value)"
    >
      {{ option.label }}
      <span
        v-if="option.count"
        class="min-w-5 rounded-full px-1.5 text-center text-xs tabular-nums"
        :class="modelValue === option.value ? 'bg-primary-500 text-white' : 'background-default text-gray-600 dark:text-gray-300'"
      >
        {{ option.count }}
      </span>
    </button>
  </div>
</template>
