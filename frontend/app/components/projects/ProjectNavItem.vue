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
  return "nav-underline py-1 inline-flex items-center gap-1.5 border-b-2 border-transparent " + (selected.value ? "nav-underline-active font-semibold " : "");
});
</script>

<template>
  <div v-if="to || href" class="mb-[-2px] mr-1">
    <!-- icons carry ~2px of transparent margin in their box, so the padding is trimmed on whichever side one sits -->
    <NuxtLink v-if="to" :to="to" class="pl-1.5 pr-2" :class="clazz">
      <slot />
    </NuxtLink>
    <a v-if="href" :href="linkout(href)" class="pl-2 pr-1.5" :class="clazz" target="_blank">
      <slot />
      <IconMdiOpenInNew class="text-xs" />
    </a>
  </div>
</template>

<style scoped>
.nav-underline {
  background-image: linear-gradient(90deg, var(--primary-500) 0%, var(--primary-400) 100%);
  background-repeat: no-repeat;
  background-origin: border-box;
  background-position: bottom;
  background-size: 0 2px;
  transition: background-size 0.25s ease;
}

.nav-underline:hover,
.nav-underline-active {
  background-size: 100% 2px;
}
</style>
