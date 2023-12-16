<script setup lang="ts">
import { useRoute } from "vue-router";
import Header from "~/components/layout/Header.vue";
import Footer from "~/components/layout/Footer.vue";
import Container from "~/components/design/Container.vue";
import Notifications from "~/components/design/Notifications.vue";

const route = useRoute();
</script>

<template>
  <main :data-page="route.name">
    <Header />
    <Container class="min-h-[80vh]">
      <div class="relative w-full">
        <slot />
      </div>
    </Container>
    <Notifications />
    <Footer />
  </main>
</template>
