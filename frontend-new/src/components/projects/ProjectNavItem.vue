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
  const routerPath = route.fullPath.endsWith("/") ? route.fullPath.substr(0, route.fullPath.length - 1) : route.fullPath;
  return routerPath == props.to;
});

const clazz = computed(() => {
  return "p-2 pb-1 mx-2 mb-1 rounded-sm border-b-3 flex items-center " + (selected.value ? "border-[#004ee9] font-semibold" : "border-neutral-400");
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
