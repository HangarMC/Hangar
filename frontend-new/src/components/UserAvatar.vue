<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    username: string;
    size?: "xs" | "sm" | "md" | "lg";
  }>(),
  {
    size: "md",
  }
);

const sizeClass = computed(() => {
  if (props.size == "xs") return "w-32px h-32px";
  else if (props.size == "sm") return "w-50px h-50px";
  else if (props.size == "md") return "w-100px h-100px";

  return "w-200px h-200px";
});
</script>

<template>
  <div :class="'bg-light-300 dark:bg-dark-500 rounded-lg ' + sizeClass">
    <span
      >{{ username }}'s<br />
      avatar</span
    >
  </div>
</template>
