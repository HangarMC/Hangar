<script setup lang="ts">
const { t } = useI18n();


</script>

<template>
  <div class="flex flex-col items-center pt-10">
      <h2 class="text-3xl font-bold uppercase">Search your favorite plugins</h2>
      <input class="mt-6 rounded-md big-box-shadow p-3 w-4/5 max-w-800px focus-visible:(border-white) text-black" type="text" placeholder="Search in 1 projects, proudly made by the community...">
  </div>
</template>

<route lang="yaml">
meta:
  layout: default
</route>

<style lang="css" scoped>

.big-box-shadow {
    box-shadow: 0 0 10px 0 #004ee99e;
}


</style>
