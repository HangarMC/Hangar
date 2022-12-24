<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";
import Header from "~/components/layout/Header.vue";
import Footer from "~/components/layout/Footer.vue";
import Container from "~/lib/components/design/Container.vue";
import Notifications from "~/lib/components/design/Notifications.vue";

const route = useRoute();
const key = computed<string>(() => route.params.user as string);
</script>

<template>
  <main :data-page="route.name">
    <Header />
    <Container class="min-h-[80vh]">
      <slot />
    </Container>
    <Notifications />
    <Footer />
  </main>
</template>
