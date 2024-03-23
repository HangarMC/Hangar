<script setup lang="ts">
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
