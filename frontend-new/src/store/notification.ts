import { defineStore } from "pinia";
import { ref } from "vue";

export interface Notification {
  color?: string;
  timeout?: number;
  message: string;
  clearable: boolean;
}

function waitTimeout(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

const defaultTimeout = 4000;
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
    await show({ message, color: "green", clearable, timeout });
  }

  async function error(message: string, clearable = true, timeout = defaultTimeout) {
    await show({ message, color: "red", clearable, timeout });
  }

  function remove(notification: Notification) {
    notifications.value.delete(notification);
  }

  return { notifications, show, remove, success, error };
});
