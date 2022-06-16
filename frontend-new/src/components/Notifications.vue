<script lang="ts" setup>
import { useNotificationStore } from "~/store/notification";

const notificationStore = useNotificationStore();
</script>

<template>
  <div class="fixed h-screen flex bottom-15 right-10 items-end pointer-events-none z-60">
    <div>
      <div
        v-for="(notification, idx) in notificationStore.notifications"
        :key="idx"
        class="rounded p-4 px-5 mb-2 pointer-events-auto text-right border-2px bg-gray-300 dark:bg-slate-700"
        :style="{ 'border-color': notification.color }"
      >
        <span class="inline-flex items-center">
          <span class="text-lg mr-2">
            <IconMdiAlertOutline v-if="notification.color === 'red'" class="text-red-600" />
            <IconMdiCheck v-else-if="notification.color === 'green'" class="text-lime-600" />
          </span>
          {{ notification.message }}
          <IconMdiClose v-if="notification.clearable" class="ml-1 cursor-pointer" @click="notificationStore.remove(notification)" />
        </span>
      </div>
    </div>
  </div>
</template>
