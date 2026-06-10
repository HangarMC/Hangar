<script lang="ts" setup>
import type { Notification } from "~/store/notification";

const notificationStore = useNotificationStore();

// handle global notifications
const clearedGlobalNotifications = useLocalStorage<string[]>("clearedGlobalNotifications", []);
const globalData = useGlobalData();
watchEffect(() => {
  if (import.meta.server) return;
  const globalNotifications = globalData.value?.globalNotifications;

  if (globalNotifications) {
    for (const key in globalNotifications) {
      if (clearedGlobalNotifications.value.includes(key)) continue;
      notificationStore.show({
        message: globalNotifications[key]!,
        color: "var(--primary-500)",
        clearable: true,
        timeout: -1,
        addedAt: Date.now(),
        key,
      });
    }
  }
});

function clearNotification(notification: Notification) {
  notificationStore.remove(notification);
  if (notification.key && globalData.value?.globalNotifications?.[notification.key]) {
    clearedGlobalNotifications.value.push(notification.key);
  }
}

function pauseNotification(notification: Notification, paused: boolean) {
  notificationStore.pause(notification, paused);
}

function notificationAccent(notification: Notification) {
  if (notification.color === "red") return "#ef4444";
  if (notification.color === "green") return "#22c55e";
  if (notification.color === "orange") return "#f59e0b";
  return notification.color || "var(--primary-500)";
}
</script>

<template>
  <div class="fixed right-4 bottom-4 z-60 flex max-w-[calc(100vw-2rem)] pointer-events-none sm:right-8 sm:bottom-8">
    <div class="w-full sm:w-auto" data-allow-mismatch="children">
      <div
        v-for="notification in notificationStore.notifications"
        :key="notification.addedAt"
        class="mb-3 w-full sm:min-w-80 sm:max-w-110"
        @mouseenter="pauseNotification(notification, true)"
        @mouseleave="pauseNotification(notification, false)"
      >
        <div
          class="background-default pointer-events-auto relative overflow-hidden rounded-xl border shadow-lg shadow-gray-300/60 dark:border-gray-800 dark:shadow-charcoal-900"
          :style="{ '--notification-accent': notificationAccent(notification) }"
        >
          <div class="flex items-center gap-3 p-3.5">
            <span class="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg notification-icon">
              <IconMdiAlertOutline v-if="notification.color === 'red'" class="text-xl" />
              <IconMdiCheck v-else-if="notification.color === 'green'" class="text-xl" />
              <IconMdiAlertCircleOutline v-else-if="notification.color === 'orange'" class="text-xl" />
              <IconMdiInformationOutline v-else class="text-xl" />
            </span>
            <!-- eslint-disable-next-line vue/no-v-html -->
            <span class="min-w-0 flex-1 text-left text-sm leading-relaxed" v-html="useDomPurify(notification.message)" />
            <button
              v-if="notification.clearable"
              class="flex h-8 w-8 flex-shrink-0 cursor-pointer items-center justify-center rounded-lg text-gray transition-colors hover:bg-gray-200 hover:text-black dark:hover:bg-gray-800 dark:hover:text-white"
              type="button"
              aria-label="Close notification"
              @click="clearNotification(notification)"
            >
              <IconMdiClose />
            </button>
          </div>
          <div v-if="notification.timeout !== -1" class="h-1 overflow-hidden bg-gray-200 dark:bg-charcoal-500">
            <div
              class="progress h-full"
              :style="{
                'animation-duration': notification.timeout + 'ms',
                'animation-play-state': notification.paused ? 'paused' : 'running',
              }"
              @animationend="clearNotification(notification)"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.notification-icon {
  color: var(--notification-accent);
  background: color-mix(in srgb, var(--notification-accent) 14%, transparent);
  border: 1px solid color-mix(in srgb, var(--notification-accent) 45%, transparent);
}

.progress {
  background-color: var(--notification-accent);
  animation-name: progress;
  animation-timing-function: linear;
}

@keyframes progress {
  0% {
    width: 100%;
  }
  100% {
    width: 0;
  }
}
</style>
