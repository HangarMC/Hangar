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
  return "px-2 py-1 inline-flex items-center " + (selected.value ? "border-b-2 border-[#004ee9] font-semibold " : "");
});
</script>

<template>
  <div class="mb-[-2px] mr-1">
    <router-link v-if="to" :to="to" :class="clazz">
      <slot></slot>
    </router-link>
    <a v-if="href" :href="props.href" :class="clazz" target="_blank">
      <span class="mx-1">
        <slot></slot>
      </span>
      <IconMdiOpenInNew class="text-xs" />
    </a>
  </div>
</template>
