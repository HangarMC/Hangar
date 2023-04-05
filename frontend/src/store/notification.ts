import { defineStore } from "pinia";
import { ref } from "vue";

export interface Notification {
  color?: string;
  timeout?: number;
  message: string;
  clearable: boolean;
  addedAt: number;
}

function waitTimeout(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

const defaultTimeout = 7_000;
export const useNotificationStore = defineStore("notification", () => {
  const notifications = ref<Set<Notification>>(new Set<Notification>());

  async function show(notification: Notification) {
    notifications.value.add(notification);
    if (notification.timeout === -1) {
      return;
    }
    await waitTimeout(notification.timeout || defaultTimeout);
    remove(notification);
  }

  async function success(message: string, clearable = true, timeout = defaultTimeout) {
    await show({ message, color: "green", clearable, timeout, addedAt: Date.now() });
  }

  async function error(message: string, clearable = true, timeout = defaultTimeout) {
    await show({ message, color: "red", clearable, timeout, addedAt: Date.now() });
  }

  async function warn(message: string, clearable = true, timeout = defaultTimeout) {
    await show({ message, color: "orange", clearable, timeout, addedAt: Date.now() });
  }

  function remove(notification: Notification) {
    notifications.value.delete(notification);
  }

  return { notifications, show, remove, success, error, warn };
});
