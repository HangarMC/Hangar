<script setup lang="ts">
import Header from "~/components/layout/Header.vue";
import Footer from "~/components/layout/Footer.vue";
import Container from "~/components/design/Container.vue";
</script>

<template>
  <main>
    <Header />
    <Container class="min-h-[80vh]">
      <transition name="slide">
        <Suspense>
          <router-view v-bind="$attrs" />
          <template #fallback> Loading...</template>
        </Suspense>
      </transition>
    </Container>
    <Footer />
  </main>
</template>
