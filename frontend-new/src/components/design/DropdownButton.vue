<script setup lang="ts">
import { Menu, MenuButton, MenuItems } from "@headlessui/vue";
import IconMdiMenuDown from "~icons/mdi/menu-down";
import IconMdiMenuUp from "~icons/mdi/menu-up";
import Button from "~/components/design/Button.vue";

const props = withDefaults(
  defineProps<{
    name?: string;
    buttonSize?: "small" | "medium" | "large";
    buttonType?: "primary" | "gray" | "red" | "transparent";
    buttonArrow: boolean;
  }>(),
  {
    name: "Dropdown",
    buttonSize: "medium",
    buttonType: "primary",
    buttonArrow: true,
  }
);
</script>

<template>
  <Menu v-slot="{ open }">
    <div>
      <MenuButton>
        <Button :button-type="props.buttonType" :size="props.buttonSize">
          <slot name="button-label">
            <span class="mx-1">{{ props.name }}</span>
          </slot>
          <template v-if="props.buttonArrow">
            <IconMdiMenuUp v-if="open" class="text-lg"></IconMdiMenuUp>
            <IconMdiMenuDown v-else class="text-lg"></IconMdiMenuDown>
          </template>
        </Button>
      </MenuButton>
      <MenuItems
        class="absolute flex flex-col mt-1 z-10 py-1 rounded border-t-2 border-primary-400 bg-background-light-0 dark:bg-background-dark-80 drop-shadow-xl"
      >
        <slot></slot>
      </MenuItems>
    </div>
  </Menu>
</template>
