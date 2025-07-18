export interface Notification {
  color?: string;
  timeout?: number;
  message: string;
  clearable: boolean;
  addedAt: number;
  key?: string;
  paused?: boolean;
}

const defaultTimeout = 7000;
export const useNotificationStore = defineStore("notification", () => {
  const notifications = ref<Set<Notification>>(new Set<Notification>());

  async function show(notification: Notification) {
    notifications.value.add(notification);
  }

  async function success(message: string, clearable = true, timeout = defaultTimeout) {
    await show({ message, color: "green", clearable, timeout, addedAt: Date.now() });
  }

  async function error(message: string, clearable = true, timeout = defaultTimeout) {
    await show({ message, color: "red", clearable, timeout, addedAt: Date.now() });
  }

  async function fromError(i18n: any, error: any, clearable = true, timeout = defaultTimeout) {
    let message = error;
    if (error.response?.data?.detail) {
      message = i18n.t(error.response.data.detail);
    }
    if (error.response?.data?.message) {
      message = i18n.t(error.response.data.message, ...error.response.data.messageArgs);
    }
    await show({ message, color: "red", clearable, timeout, addedAt: Date.now() });
  }

  async function warn(message: string, clearable = true, timeout = defaultTimeout) {
    await show({ message, color: "orange", clearable, timeout, addedAt: Date.now() });
  }

  function remove(notification: Notification) {
    notifications.value.delete(notification);
  }

  function pause(notification: Notification, paused: boolean) {
    notification.paused = paused;
  }

  return { notifications, show, remove, success, error, fromError, warn, pause };
});
