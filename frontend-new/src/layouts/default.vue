<script setup lang="ts">
import Header from "~/components/Header.vue";
import Footer from "~/components/Footer.vue";
</script>

<template>
  <main style="min-height: 60vh">
    <Header />
    <div style="min-height: 60vh">
      <router-view v-bind="$attrs" />
    </div>
    <Footer />
  </main>
</template>
