<script setup lang="ts">
import { useRoute } from "vue-router";
import { computed } from "vue";
import { linkout } from "~/composables/useUrlHelper";

const props = defineProps<{
  to?: string;
  href?: string;
  icon?: string;
}>();

const route = useRoute();

const selected = computed(() => {
  const routerPath = route.fullPath.endsWith("/") ? route.fullPath.substr(0, route.fullPath.length - 1) : route.fullPath;
  return routerPath === props.to;
});

const clazz = computed(() => {
  return (
    "px-2 py-1 inline-flex items-center transition duration-300 border-b-2 border-transparent hover:border-[#004ee9] " +
    (selected.value ? "!border-[#004ee9] font-semibold " : "")
  );
});
</script>

<template>
  <div v-if="to || href" class="mb-[-2px] mr-1">
    <NuxtLink v-if="to" :to="to" :class="clazz">
      <slot></slot>
    </NuxtLink>
    <a v-if="href" :href="linkout(href)" :class="clazz" target="_blank">
      <span class="mx-1">
        <slot></slot>
      </span>
      <IconMdiOpenInNew class="text-xs" />
    </a>
  </div>
</template>
