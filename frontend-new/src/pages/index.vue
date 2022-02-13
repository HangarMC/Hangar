<script setup lang="ts">
const { t } = useI18n();
</script>

<template>
  <div>Index Page {{ t("general.close") }}</div>
</template>

<route lang="yaml">
meta:
  layout: default
</route>
