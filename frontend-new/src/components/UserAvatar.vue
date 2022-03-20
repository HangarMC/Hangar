<script setup lang="ts">
const props = defineProps<{
  username: string;
}>();
</script>

<template>
  <span
    >{{ username }}'s<br />
    avatar</span
  >
</template>
