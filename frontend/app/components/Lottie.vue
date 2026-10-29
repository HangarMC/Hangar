<script lang="ts" setup>
defineProps<{
  src: string;
}>();

const emit = defineEmits<{
  error: [];
}>();

if (!import.meta.env.SSR) {
  onMounted(() => {
    const existingScript = document.querySelector<HTMLScriptElement>("[data-lottie-loaded=true]");
    if (existingScript) {
      if (existingScript.dataset.lottieError === "true") emit("error");
      return;
    }
    const script = document.createElement("script");
    script.setAttribute("src", "https://unpkg.com/@lottiefiles/lottie-player@1.5.7/dist/lottie-player.js");
    script.dataset.lottieLoaded = "true";
    script.addEventListener("error", () => {
      script.dataset.lottieError = "true";
      emit("error");
    });
    document.head.append(script);
  });
}
</script>

<template>
  <ClientOnly>
    <lottie-player autoplay loop mode="normal" :src="src" @error="emit('error')" />
  </ClientOnly>
</template>
