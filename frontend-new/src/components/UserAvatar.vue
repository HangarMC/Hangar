<script setup lang="ts">
import { computed, ref } from "vue";

const props = withDefaults(
  defineProps<{
    username: string;
    avatarUrl?: string;
    imgSrc?: string;
    to?: string;
    size?: "xs" | "sm" | "md" | "lg" | "xl";
  }>(),
  {
    avatarUrl: undefined,
    imgSrc: undefined,
    size: "lg",
    to: "",
  }
);

const errored = ref(false);

const sizeClass = computed(() => {
  if (props.size == "xs") return "w-32px h-32px";
  else if (props.size == "sm") return "w-50px h-50px";
  else if (props.size == "md") return "w-75px h-75px";
  else if (props.size == "lg") return "w-100px h-100px";

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
    return "";
  }
});

const url = computed(() => {
  if (props.to) {
    return props.to;
  } else if (props.username) {
    return "/" + props.username;
  } else {
    return "#";
  }
});
</script>

<template>
  <div :class="'bg-light-300 dark:bg-dark-500 rounded-lg ' + sizeClass">
    <router-link :to="url">
      <img class="rounded-lg w-full h-full" :title="username" :src="src" :alt="username" @error="errored = true" />
    </router-link>
  </div>
</template>
