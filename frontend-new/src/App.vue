<script setup lang="ts">
import { useSettingsStore } from "~/store/settings";

useSettingsStore().loadSettingsClient();
</script>

<template>
  <router-view v-slot="{ Component, route }">
    <component :is="Component" :key="route" />
  </router-view>
</template>
