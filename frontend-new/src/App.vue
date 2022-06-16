<script setup lang="ts">
import { useSettingsStore } from "~/store/settings";

useSettingsStore().loadSettingsClient();
</script>

<template>
  <router-view v-slot="{ Component }">
    <Suspense>
      <component :is="Component" />
      <template #fallback> Loading... </template>
    </Suspense>
  </router-view>
</template>
