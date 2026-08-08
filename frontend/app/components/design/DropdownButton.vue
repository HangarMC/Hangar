<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    name?: string;
    buttonSize?: "sm" | "md" | "lg";
    buttonVariant?: "solid" | "outline" | "ghost";
    buttonTone?: "primary" | "neutral" | "danger";
    buttonArrow?: boolean;
    placement?: "bottom" | "top" | "left" | "right" | "bottom-end" | "bottom-start";
    /** `false` renders the popper next to its trigger instead of teleporting to `body`, needed inside a native `<dialog>`. */
    container?: string | false;
    /** Positioning strategy passed through to floating-vue; use `"fixed"` alongside `container: false` inside a `<dialog>`. */
    strategy?: "absolute" | "fixed";
  }>(),
  {
    name: "Dropdown",
    buttonSize: "md",
    buttonVariant: "solid",
    buttonTone: "primary",
    buttonArrow: true,
    placement: "bottom-end",
    container: "body",
    strategy: "absolute",
  }
);
</script>

<template>
  <Popper :placement="placement" :container="container" :strategy="strategy">
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
