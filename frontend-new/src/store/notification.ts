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

export const useNotificationStore = defineStore("notification", () => {
  const notifications = ref<Set<Notification>>(new Set<Notification>());

  async function show(notification: Notification) {
    notifications.value.add(notification);
    await waitTimeout(notification.timeout || 3000);
    remove(notification);
  }

  async function success(message: string, clearable = true) {
    await show({ message, color: "green", clearable });
  }

  async function error(message: string, clearable = true) {
    await show({ message, color: "red", clearable });
  }

  function remove(notification: Notification) {
    notifications.value.delete(notification);
  }

  return { notifications, show, remove, success, error };
});
