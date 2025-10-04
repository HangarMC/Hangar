<script setup lang="ts">
import type { Announcement } from "#shared/types/backend";

const props = defineProps<{ announcement: Announcement }>();
const { announcement } = toRefs(props);
</script>

<template>
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div :style="'background-color:' + announcement.color + '60; border-color:' + announcement.color" class="py-2 px-20 text-center text-black dark:text-white rounded-2xl border-1" v-html="useDomPurify(announcement.text)" />
</template>
