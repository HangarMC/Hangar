<script lang="ts" setup>
import { ref } from "vue";

defineProps<{
  values: any[];
}>();

const expanded = ref<Record<number, boolean>>({});

function toggle(index: number) {
  expanded.value[index] = !expanded.value[index];
}
</script>

<template>
  <ul>
    <li v-for="(entry, idx) in values" :key="idx" class="border p-2">
      <div class="flex flex-row items-center">
        <div class="cursor-pointer flex-grow" @click="toggle(idx)">
          <slot name="header" :entry="entry" :index="idx" />
        </div>
        <slot name="plainHeader" :entry="entry" :index="idx" />
      </div>
      <div v-if="expanded[idx]" class="p-2">
        <hr />
        <slot name="entry" :entry="entry" :index="idx" />
      </div>
    </li>
  </ul>
</template>
