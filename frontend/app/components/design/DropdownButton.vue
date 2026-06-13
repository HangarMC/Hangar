<script setup lang="ts">
defineEmits<{
  click: [event: MouseEvent];
}>();

const props = withDefaults(
  defineProps<{
    name?: string;
    buttonSize?: "small" | "medium" | "large";
    buttonType?: "primary" | "red" | "transparent" | "secondary" | "borderless";
    buttonArrow?: boolean;
    placement?: "bottom" | "top" | "left" | "right" | "bottom-end" | "bottom-start";
    isSortBy?: boolean;
    matchWidth?: boolean;
    matchMenuWidth?: boolean;
    buttonClass?: string;
    spreadArrow?: boolean;
  }>(),
  {
    name: "Dropdown",
    buttonSize: "medium",
    buttonType: "primary",
    buttonArrow: true,
    placement: "bottom-end",
    isSortBy: false,
    matchWidth: false,
    matchMenuWidth: false,
    buttonClass: "",
    spreadArrow: false,
  }
);
</script>

<template>
  <Popper :placement="placement" :auto-size="props.matchWidth || props.matchMenuWidth" :class="{ '!w-full': props.matchWidth }">
    <template #default="{ shown }">
      <Button
        class="h-10.5"
        :class="[props.buttonClass, { 'min-w-50': props.isSortBy, '!w-full !justify-between': props.matchWidth }]"
        :button-type="props.buttonType"
        :size="props.buttonSize"
        @click="$emit('click', $event)"
      >
        <span class="inline-flex items-center justify-center" :class="{ 'flex-1': props.spreadArrow }">
          <slot name="button-label">
            <span class="mx-1">{{ props.name }}</span>
          </slot>
        </span>
        <template v-if="props.buttonArrow">
          <IconMdiChevronDown v-if="shown" class="text-lg flex-shrink-0" />
          <IconMdiChevronUp v-else class="text-lg flex-shrink-0" />
        </template>
      </Button>
    </template>
    <template #content="{ close }">
      <div class="background-default z-10 flex max-h-72 flex-col gap-1 overflow-y-auto rounded-lg border-1 border-gray-800 p-1 shadow-lg shadow-charcoal-900">
        <slot :close="close" />
      </div>
    </template>
  </Popper>
</template>
