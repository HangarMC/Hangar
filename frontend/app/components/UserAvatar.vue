<script setup lang="ts">
import { NuxtLink } from "#components";

const props = withDefaults(
  defineProps<{
    username?: string;
    avatarUrl?: string;
    imgSrc?: string;
    to?: string;
    /**
    `fill` sizes to the parent box -- use it when the surrounding layout dictates the height.
    */
    size?: "xs" | "sm" | "md" | "lg" | "xl" | "fill";
    disableLink?: boolean;
    loading?: boolean;
    /**
    Name the monogram is derived from when there is no uploaded avatar. Defaults to `username`.
    */
    monogramName?: string;
  }>(),
  {
    username: undefined,
    avatarUrl: undefined,
    imgSrc: undefined,
    size: "lg",
    to: "",
    disableLink: false,
    loading: false,
    monogramName: undefined,
  }
);

const errored = ref(false);

const sizeClass = computed(() => {
  switch (props.size) {
    case "xs":
      return "w-32px h-32px text-xs";
    case "sm":
      return "w-50px h-50px text-base";
    case "md":
      return "w-75px h-75px text-2xl";
    case "lg":
      return "w-100px h-100px text-3xl";
    case "fill":
      return "w-full h-full avatar-fill";
    // No default
  }

  return "w-200px h-200px text-6xl";
});

const src = computed(() => props.imgSrc || props.avatarUrl);
const name = computed(() => props.monogramName || props.username);
const showMonogram = computed(() => errored.value || isDefaultAvatar(src.value));

const url = computed(() => {
  if (props.disableLink) {
    return;
  }
  if (props.to) {
    return props.to;
  }
  return props.username ? "/" + props.username : "#";
});
</script>

<template>
  <div :class="'rounded-lg ' + sizeClass">
    <component :is="disableLink ? 'span' : NuxtLink" :key="url" :to="url">
      <Skeleton v-if="loading" class="rounded-lg w-full h-full" />
      <div
        v-else-if="showMonogram"
        class="monogram w-full h-full flex items-center justify-center rounded-lg font-bold tracking-tight text-white select-none"
        :style="{ background: monogramBackground(name) }"
        :title="name"
        role="img"
        :aria-label="'Avatar for ' + name"
      >
        {{ monogramInitials(name) }}
      </div>
      <img v-else class="rounded-lg w-full h-full" :title="username" :src="src" :alt="'Avatar for ' + username" @error="errored = true" />
    </component>
  </div>
</template>

<style scoped>
.avatar-fill {
  container-type: size;
}

.avatar-fill .monogram {
  font-size: 32cqmin;
}
</style>
