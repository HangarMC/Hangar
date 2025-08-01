<script setup lang="ts">
import { NuxtLink } from "#components";

const props = withDefaults(
  defineProps<{
    username?: string;
    avatarUrl?: string;
    imgSrc?: string;
    to?: string;
    size?: "xs" | "sm" | "md" | "lg" | "xl";
    disableLink?: boolean;
    loading?: boolean;
  }>(),
  {
    username: undefined,
    avatarUrl: undefined,
    imgSrc: undefined,
    size: "lg",
    to: "",
    disableLink: false,
    loading: false,
  }
);

const errored = ref(false);

const sizeClass = computed(() => {
  switch (props.size) {
    case "xs":
      return "w-32px h-32px";
    case "sm":
      return "w-50px h-50px";
    case "md":
      return "w-75px h-75px";
    case "lg":
      return "w-100px h-100px";
    case "xl":
      return "w-125px h-125px";
    // No default
  }

  return "w-200px h-200px";
});

const src = computed(() => {
  if (errored.value) {
    return "https://docs.papermc.io/img/paper.png";
  } else if (props.imgSrc) {
    return props.imgSrc;
  } else if (props.avatarUrl) {
    return props.avatarUrl;
  } else {
    return "https://docs.papermc.io/img/paper.png";
  }
});

const url = computed(() => {
  if (props.disableLink) {
    return;
  } else if (props.to) {
    return props.to;
  } else if (props.username) {
    return "/" + props.username;
  } else {
    return "#";
  }
});
</script>

<template>
  <div :class="'rounded-lg ' + sizeClass">
    <component :is="disableLink ? 'span' : NuxtLink" :key="url" :to="url">
      <Skeleton v-if="loading" class="rounded-lg w-full h-full" />
      <img v-else class="rounded-lg w-full h-full" :title="username" :src="src" :alt="'Avatar for ' + username" @error="errored = true" />
    </component>
  </div>
</template>
