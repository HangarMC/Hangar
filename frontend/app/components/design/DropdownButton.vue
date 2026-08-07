<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    name?: string;
    buttonSize?: "sm" | "md" | "lg";
    buttonVariant?: "solid" | "outline" | "ghost";
    buttonTone?: "primary" | "neutral" | "danger";
    buttonArrow?: boolean;
    placement?: "bottom" | "top" | "left" | "right" | "bottom-end" | "bottom-start";
  }>(),
  {
    name: "Dropdown",
    buttonSize: "md",
    buttonVariant: "solid",
    buttonTone: "primary",
    buttonArrow: true,
    placement: "bottom-end",
  }
);
</script>

<template>
  <Popper :placement="placement">
    <template #default="{ shown }">
      <Button :variant="props.buttonVariant" :tone="props.buttonTone" :size="props.buttonSize">
        <slot name="button-label">
          {{ props.name }}
        </slot>
        <IconMdiChevronDown v-if="props.buttonArrow" class="transition-transform" :class="{ 'rotate-180': shown }" />
      </Button>
    </template>
    <template #content="{ close }">
      <DropdownPanel>
        <slot :close="close" />
      </DropdownPanel>
    </template>
  </Popper>
</template>
