<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    name?: string;
    buttonSize?: "small" | "medium" | "large";
    buttonType?: "primary" | "red" | "transparent" | "secondary";
    buttonArrow?: boolean;
    placement?: "bottom" | "top" | "left" | "right" | "bottom-end" | "bottom-start";
    isSortBy?: boolean;
  }>(),
  {
    name: "Dropdown",
    buttonSize: "medium",
    buttonType: "primary",
    buttonArrow: true,
    placement: "bottom-end",
    isSortBy: false,
  }
);
</script>

<template>
  <Popper :placement="placement">
    <template #default="{ shown }">
      <Button class="h-10.5" :class="{ 'min-w-50': props.isSortBy }" :button-type="props.buttonType" :size="props.buttonSize">
        <slot name="button-label">
          <span class="mx-1">{{ props.name }}</span>
        </slot>
        <template v-if="props.buttonArrow">
          <IconMdiChevronDown v-if="shown" class="text-lg" />
          <IconMdiChevronUp v-else class="text-lg" />
        </template>
      </Button>
    </template>
    <template #content="{ close }">
      <div class="flex flex-col z-10 py-1 rounded-xl border-1 border-gray-800 background-default shadow-default">
        <slot :close="close" />
      </div>
    </template>
  </Popper>
</template>
