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
</script>

<template>
  <div class="fixed h-screen flex bottom-15 right-10 items-end pointer-events-none z-60 text-right">
    <div data-allow-mismatch="children">
      <div
        v-for="notification in notificationStore.notifications"
        :key="notification.addedAt"
        class="mb-3"
        @mouseenter="pauseNotification(notification, true)"
        @mouseleave="pauseNotification(notification, false)"
      >
        <div
          class="rounded-t p-4 pb-2 px-3 pointer-events-auto text-right border-l-5px bg-gray-300 dark:bg-slate-700"
          :style="{ 'border-color': notification.color }"
        >
          <div class="inline-flex items-center">
            <span class="text-lg mr-2">
              <IconMdiAlertOutline v-if="notification.color === 'red'" class="text-red-600" />
              <IconMdiCheck v-else-if="notification.color === 'green'" class="text-lime-600" />
              <IconMdiInformation v-else-if="notification.color === 'orange'" class="text-red-400" />
            </span>
            <!-- eslint-disable-next-line vue/no-v-html -->
            <span v-html="useDomPurify(notification.message)" />
            <IconMdiClose v-if="notification.clearable" class="ml-2 cursor-pointer" @click="clearNotification(notification)" />
          </div>
          <div v-if="notification.timeout == -1" class="mb-2" />
        </div>
        <div v-if="notification.timeout !== -1" class="bar">
          <div
            class="progress"
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
</template>

<style lang="scss" scoped>
.bar {
  width: 100%;
  background-color: gray;
}

.progress {
  height: 5px;
  background-color: lightblue;
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
