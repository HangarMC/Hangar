<script setup lang="ts">
const props = defineProps<{
  to?: string;
  href?: string;
  icon?: string;
  title?: string;
  compact?: boolean;
}>();

const route = useRoute();

const selected = computed(() => {
  const routerPath = route.path.endsWith("/") ? route.path.slice(0, Math.max(0, route.path.length - 1)) : route.path;
  return routerPath === props.to;
});

const clazz = computed(() => {
  return (
    "h-9 inline-flex items-center justify-center rounded-lg border leading-none font-semibold transition-all duration-250 hover:scale-[1.005] hover:bg-gray-200 hover:border-gray-300 dark:hover:bg-gray-800 dark:hover:border-gray-700 " +
    (props.compact ? "px-3 " : "px-4 ") +
    (selected.value ? "border-primary-500 " : "border-transparent ")
  );
});
</script>

<template>
  <div v-if="to || href" class="min-w-0 max-w-44 flex-shrink-0">
    <NuxtLink
      v-if="to"
      :to="to"
      :class="clazz"
      :style="
        selected
          ? {
              backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
              borderColor: 'var(--primary-500)',
            }
          : {}
      "
    >
      <span class="truncate" :title="title">
        <slot />
      </span>
    </NuxtLink>
    <a v-if="href" :href="linkout(href)" :class="clazz" target="_blank">
      <span class="truncate" :title="title">
        <slot />
      </span>
      <IconMdiOpenInNew class="ml-1 text-xs" />
    </a>
  </div>
</template>
