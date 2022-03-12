<script setup lang="ts">
import Header from "~/components/Header.vue";
import Footer from "~/components/Footer.vue";
</script>

<template>
  <main>
    <Header />
    <div class="min-h-[60vh]">
      <Suspense>
        <router-view v-bind="$attrs" />
        <template #fallback> Loading...</template>
      </Suspense>
    </div>
    <Footer />
  </main>
</template>
