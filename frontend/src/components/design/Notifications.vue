<script lang="ts" setup>
import { useNotificationStore } from "~/store/notification";

const notificationStore = useNotificationStore();

// TODO make more responsive: stop removal timer and progress bar on hover
</script>

<template>
  <div class="fixed h-screen flex bottom-15 right-10 items-end pointer-events-none z-60 text-right">
    <div>
      <div v-for="notification in notificationStore.notifications" :key="notification.addedAt" class="mb-3">
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
            {{ notification.message }}
            <IconMdiClose v-if="notification.clearable" class="ml-2 cursor-pointer" @click="notificationStore.remove(notification)" />
          </div>
        </div>
        <div class="bar">
          <div class="progress" :style="'animation-duration: ' + notification.timeout + 'ms'"></div>
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
  animation-duration: 7s;
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
