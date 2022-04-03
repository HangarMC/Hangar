<script setup lang="ts">
import { Menu, MenuButton, MenuItems } from "@headlessui/vue";
import IconMdiMenuDown from "~icons/mdi/menu-down";
import IconMdiMenuUp from "~icons/mdi/menu-up";

const props = withDefaults(
  defineProps<{
    name?: string;
  }>(),
  {
    name: "Dropdown",
  }
);
</script>

<template>
  <Menu v-slot="{ open }">
    <div>
      <MenuButton class="p-2 rounded bg-primary-400 text-white font-semibold inline-flex items-center" hover="bg-primary-0 text-primary-400">
        <span class="mx-1">{{ props.name }}</span>
        <IconMdiMenuUp v-if="open" class="text-lg"></IconMdiMenuUp>
        <IconMdiMenuDown v-else class="text-lg"></IconMdiMenuDown>
      </MenuButton>
      <MenuItems
        class="absolute flex flex-col mt-1 z-10 py-1 rounded border-t-2 border-primary-400 bg-background-light-0 dark:bg-background-dark-80 drop-shadow-xl"
      >
        <slot></slot>
      </MenuItems>
    </div>
  </Menu>
</template>
