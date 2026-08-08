<script lang="ts" setup>
import type { Notification } from "~/store/notification";

const notificationStore = useNotificationStore();
const { t } = useI18n();

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
        color: "info",
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

type Severity = "success" | "error" | "warning" | "info";

// the store only records a bare color keyword; map it to a severity here for styling/icons/a11y
const severityByColor: Record<string, Severity> = {
  green: "success",
  red: "error",
  orange: "warning",
};

function severityOf(notification: Notification): Severity {
  return severityByColor[notification.color ?? ""] ?? "info";
}

// addedAt can collide when several notifications fire within the same millisecond, so :key needs its own identity
const notificationIds = new WeakMap<Notification, number>();
let nextNotificationId = 0;
function keyOf(notification: Notification): number {
  let id = notificationIds.get(notification);
  if (id === undefined) {
    id = nextNotificationId++;
    notificationIds.set(notification, id);
  }
  return id;
}

const severityStyles: Record<Severity, { surface: string; puck: string; bar: string }> = {
  success: {
    surface: "border-green-300 dark:border-green-500/40 bg-green-50 dark:bg-green-500/10",
    puck: "bg-green-500/15 text-green-700 dark:text-green-400",
    bar: "bg-green-500",
  },
  error: {
    surface: "border-red-300 dark:border-red-500/40 bg-red-50 dark:bg-red-500/10",
    puck: "bg-red-500/15 text-red-700 dark:text-red-400",
    bar: "bg-red-500",
  },
  warning: {
    surface: "border-amber-300 dark:border-amber-500/40 bg-amber-50 dark:bg-amber-500/10",
    puck: "bg-amber-500/15 text-amber-700 dark:text-amber-500",
    bar: "bg-amber-500",
  },
  info: {
    surface: "border-sky-300 dark:border-sky-500/40 bg-sky-50 dark:bg-sky-500/10",
    puck: "bg-sky-500/15 text-sky-700 dark:text-sky-400",
    bar: "bg-sky-500",
  },
};
</script>

<template>
  <div
    class="toast-stack fixed z-60 bottom-4 left-4 right-4 sm:left-auto sm:right-10 sm:bottom-15 flex flex-col items-end gap-3 pointer-events-none max-h-[calc(100vh-2rem)]"
  >
    <TransitionGroup name="toast" tag="div" class="flex flex-col items-end gap-3 w-full" data-allow-mismatch="children">
      <div
        v-for="notification in notificationStore.notifications"
        :key="keyOf(notification)"
        class="toast-move w-full sm:w-96 max-w-full rounded-lg border pointer-events-auto shadow-lg overflow-hidden text-left"
        :class="severityStyles[severityOf(notification)].surface"
        :role="severityOf(notification) === 'error' ? 'alert' : 'status'"
        :aria-live="severityOf(notification) === 'error' ? 'assertive' : 'polite'"
        @mouseenter="pauseNotification(notification, true)"
        @mouseleave="pauseNotification(notification, false)"
      >
        <div class="flex items-start gap-3 p-3">
          <span class="flex-none flex items-center justify-center w-8 h-8 rounded-full" :class="severityStyles[severityOf(notification)].puck">
            <IconMdiCheckCircle v-if="severityOf(notification) === 'success'" class="text-lg" />
            <IconMdiAlertCircle v-else-if="severityOf(notification) === 'error'" class="text-lg" />
            <IconMdiAlert v-else-if="severityOf(notification) === 'warning'" class="text-lg" />
            <IconMdiInformation v-else class="text-lg" />
          </span>
          <!-- eslint-disable-next-line vue/no-v-html -->
          <div class="flex-1 min-w-0 pt-1 text-sm leading-snug break-words whitespace-pre-line text-gray-900 dark:text-gray-100" v-html="useDomPurify(notification.message)" />
          <Button
            v-if="notification.clearable"
            variant="ghost"
            tone="neutral"
            size="sm"
            icon-only
            :aria-label="t('general.close')"
            class="flex-none -mr-1.5 -mt-1"
            @click="clearNotification(notification)"
          >
            <IconMdiClose />
          </Button>
        </div>
        <div v-if="notification.timeout !== -1" class="h-1 bg-black/5 dark:bg-white/10">
          <div
            class="h-full progress"
            :class="severityStyles[severityOf(notification)].bar"
            :style="{
              'animation-duration': notification.timeout + 'ms',
              'animation-play-state': notification.paused ? 'paused' : 'running',
            }"
            @animationend="clearNotification(notification)"
          />
        </div>
      </div>
    </TransitionGroup>
  </div>
</template>

<style lang="scss" scoped>
/* clip, not auto: a scroll container grows scrollbars whenever a transition transform leaves the box */
.toast-stack {
  overflow: clip;
  overflow-clip-margin: 24px;
}

.progress {
  animation-name: progress;
  animation-timing-function: linear;
  animation-fill-mode: forwards;
}

@keyframes progress {
  0% {
    width: 100%;
  }
  100% {
    width: 0;
  }
}

.toast-move,
.toast-enter-active,
.toast-leave-active {
  transition:
    transform 220ms ease,
    opacity 220ms ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(24px);
}

@media (prefers-reduced-motion: reduce) {
  .progress,
  .toast-move,
  .toast-enter-active,
  .toast-leave-active {
    transition: none;
    animation: none;
  }
}
</style>
