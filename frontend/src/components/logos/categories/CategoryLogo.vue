<script lang="ts" setup>
import { ProjectCategory } from "~/types/enums";

withDefaults(
  defineProps<{
    category: ProjectCategory;
    size?: number;
  }>(),
  {
    size: 72,
  }
);
</script>
<template>
  <icon-mdi-tools v-if="category === ProjectCategory.ADMIN_TOOLS" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-chat v-else-if="category === ProjectCategory.CHAT" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-code-braces-box v-else-if="category === ProjectCategory.DEV_TOOLS" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-cash-multiple v-else-if="category === ProjectCategory.ECONOMY" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-controller v-else-if="category === ProjectCategory.GAMEPLAY" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-gamepad-round-left v-else-if="category === ProjectCategory.GAMES" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-shield-sun v-else-if="category === ProjectCategory.PROTECTION" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-horse-variant v-else-if="category === ProjectCategory.ROLE_PLAYING" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-earth v-else-if="category === ProjectCategory.WORLD_MANAGEMENT" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-shape v-else-if="category === ProjectCategory.MISC" v-bind="$attrs" :width="size" :height="size" />
</template>
