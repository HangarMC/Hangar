<script lang="ts" setup>
defineProps<{
  src: string;
}>();

if (!import.meta.env.SSR) {
  onMounted(() => {
    if (document.querySelector("[data-lottie-loaded=true]")) return;
    const script = document.createElement("script");
    script.setAttribute("src", "https://unpkg.com/@lottiefiles/lottie-player@1.5.7/dist/lottie-player.js");
    script.dataset.lottieLoaded = "true";
    document.head.append(script);
  });
}
</script>

<template>
  <ClientOnly>
    <lottie-player autoplay loop mode="normal" :src="src" />
  </ClientOnly>
</template>
