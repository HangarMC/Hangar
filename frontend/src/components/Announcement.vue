<script setup lang="ts">
import type { Announcement } from "hangar-api";
import { toRefs } from "vue";
import { useDomPurify } from "~/composables/useDomPurify";

const props = defineProps<{ announcement: Announcement }>();
const { announcement } = toRefs(props);
</script>

<template>
  <!-- eslint-disable-next-line vue/no-v-html -->
  <div :style="'background-color:' + announcement.color" class="p-2 text-center text-white" v-html="useDomPurify(announcement.text)" />
</template>
