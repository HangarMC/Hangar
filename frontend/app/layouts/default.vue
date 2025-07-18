<script setup lang="ts">
const route = useRoute();
const i18n = useI18n();

function fixCrowdin() {
  document.documentElement.classList.remove("jipt-no-scroll");
  document.body.classList.remove("jipt-highlight");
  document.getElementById("crowdin-jipt-mask")?.remove();
  document.getElementById("jipt-modal-mask")?.remove();
  // eslint-disable-next-line unicorn/no-array-for-each
  document.querySelectorAll(".crowdin-jipt")?.forEach((e) => e.remove());
}
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
    <button v-if="i18n.locale.value === 'dum'" class="crowdin-fix" aria-hidden="true" @click="fixCrowdin">✖</button>
  </main>
</template>

<style lang="scss" scoped>
.crowdin-fix {
  display: none;
}
.jipt-no-scroll .crowdin-fix {
  position: absolute;
  display: block;
  top: 10px;
  right: 10px;
  z-index: 1000000000000000000;
  color: black;
  font-size: 26px;
}
</style>
