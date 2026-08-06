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
        <template v-if="props.buttonArrow">
          <IconMdiMenu v-if="shown" />
          <IconMdiMenuDown v-else />
        </template>
      </Button>
    </template>
    <template #content="{ close }">
      <div class="flex flex-col z-10 py-1 rounded border-t-2 border-primary-500 background-default shadow-default">
        <slot :close="close" />
      </div>
    </template>
  </Popper>
</template>
