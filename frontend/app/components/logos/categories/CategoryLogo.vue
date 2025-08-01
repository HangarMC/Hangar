<script lang="ts" setup>
import { Category } from "#shared/types/backend";

withDefaults(
  defineProps<{
    category: Category;
    size?: number;
  }>(),
  {
    size: 72,
  }
);
</script>
<template>
  <icon-mdi-tools v-if="category === Category.AdminTools" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-chat v-else-if="category === Category.Chat" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-code-braces-box v-else-if="category === Category.DevTools" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-cash-multiple v-else-if="category === Category.Economy" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-controller v-else-if="category === Category.Gameplay" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-gamepad-round-left v-else-if="category === Category.Games" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-shield-sun v-else-if="category === Category.Protection" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-horse-variant v-else-if="category === Category.RolePlaying" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-earth v-else-if="category === Category.WorldManagement" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-shape v-else-if="category === Category.Misc" v-bind="$attrs" :width="size" :height="size" />
</template>
