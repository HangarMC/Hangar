<script setup lang="ts">
const props = defineProps<{
  to?: string;
  href?: string;
  icon?: string;
}>();

const route = useRoute();

const selected = computed(() => {
  const routerPath = route.path.endsWith("/") ? route.path.slice(0, Math.max(0, route.path.length - 1)) : route.path;
  return routerPath === props.to;
});

const clazz = computed(() => {
  return (
    "px-2 py-1 inline-flex items-center transition duration-300 border-b-2 border-transparent hover:border-primary-500 " +
    (selected.value ? "!border-primary-500 font-semibold " : "")
  );
});
</script>

<template>
  <div v-if="to || href" class="mb-[-2px] mr-1">
    <NuxtLink v-if="to" :to="to" :class="clazz">
      <slot />
    </NuxtLink>
    <a v-if="href" :href="linkout(href)" :class="clazz" target="_blank">
      <span class="mx-1">
        <slot />
      </span>
      <IconMdiOpenInNew class="text-xs" />
    </a>
  </div>
</template>
