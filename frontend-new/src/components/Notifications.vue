<script lang="ts" setup>
import { useNotificationStore } from "~/store/notification";

const notificationStore = useNotificationStore();
</script>

<template>
  <div class="fixed h-screen flex bottom-0 left-0 right-0 items-end justify-center pointer-events-none z-60">
    <div>
      <div
        v-for="(notification, idx) in notificationStore.notifications"
        :key="idx"
        class="rounded p-4 mb-2 pointer-events-auto"
        :style="{ 'background-color': notification.color }"
      >
        <span class="inline-flex items-center">
          {{ notification.message }}
          <IconMdiClose v-if="notification.clearable" class="ml-1 cursor-pointer" @click="notificationStore.remove(notification)" />
        </span>
      </div>
    </div>
  </div>
</template>
