<script setup lang="ts">
import { useRoute } from "vue-router";
import { computed } from "vue";

const props = defineProps<{
  to?: string;
  href?: string;
  icon?: string;
}>();

const route = useRoute();

const selected = computed(() => {
  return route.fullPath == props.to;
});

const clazz = computed(() => {
  return "p-2 pb-1 mx-2 mb-1 rounded-sm border-b-3 flex items-center " + [selected.value ? "border-[#004ee9] font-medium" : "border-neutral-400"];
});
</script>

<template>
  <router-link v-if="to" :to="to" :class="clazz">
    <slot></slot>
  </router-link>
  <a v-if="href" :href="props.href" :class="clazz" target="_blank">
    <span class="mx-1">
      <slot></slot>
    </span>
    <IconMdiOpenInNew class="text-xs" />
  </a>
</template>
