<script setup lang="ts">
const props = defineProps<{
  to?: string;
  label: string;
  active?: boolean;
}>();

const clazz = computed(() => {
  return "nav-underline py-1 inline-flex items-center gap-1.5 border-b-2 border-transparent " + (props.active ? "nav-underline-active font-semibold " : "");
});
</script>

<template>
  <div v-if="to" class="mb-[-2px] mr-1">
    <!-- icons carry ~2px of transparent margin in their box, so the padding is trimmed on whichever side one sits -->
    <NuxtLink :to="to" class="pl-1.5 pr-2" :class="clazz">
      <slot />
      <span class="nav-label" :data-label="label">{{ label }}</span>
    </NuxtLink>
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

/* a hidden bold copy holds the width, so selecting a tab can't nudge the ones after it */
.nav-label {
  display: inline-grid;
  justify-items: center;
}

.nav-label::after {
  content: attr(data-label);
  height: 0;
  overflow: hidden;
  visibility: hidden;
  font-weight: 600;
}
</style>
