<script setup lang="ts">
import type { Announcement } from "hangar-api";
import { toRefs } from "vue";

const props = defineProps<{ announcement: Announcement }>();
const { announcement } = toRefs(props);
</script>

<template>
  <div :style="'background-color:' + announcement.color" class="p-2 text-center text-white" v-dompurify-html="announcement.text" />
</template>
