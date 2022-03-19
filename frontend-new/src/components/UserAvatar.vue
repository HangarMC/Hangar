<script setup lang="ts">
const props = defineProps<{
  username: string;
}>();
</script>

<template>
  <span>{{ username }}'s avatar</span>
</template>
