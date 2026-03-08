import type {
  PaginatedResultHangarNotification,
  Invites,
} from "#shared/types/backend";

export function useUnreadNotificationsQuery() {
  return useQuery({
    key: () => ["unreadNotifications"] as const,
    query: () => useInternalApi<PaginatedResultHangarNotification>("unreadnotifications"),
    staleTime: 30_000,
  });
}

export function useReadNotificationsQuery() {
  return useQuery({
    key: () => ["readNotifications"] as const,
    query: () => useInternalApi<PaginatedResultHangarNotification>("readnotifications"),
    staleTime: 30_000,
  });
}

export function useNotificationsQuery() {
  return useQuery({
    key: () => ["notifications"] as const,
    query: () => useInternalApi<PaginatedResultHangarNotification>("notifications"),
    staleTime: 30_000,
  });
}

export function useUnreadCountQuery() {
  const authStore = useAuthStore();
  return useQuery({
    key: () => ["unreadCount"] as const,
    query: () => useInternalApi<{ notifications: number; invites: number }>("unreadcount"),
    enabled: () => !!authStore.user,
    placeholderData: () => authStore.user?.headerData?.unreadCount ?? { notifications: 0, invites: 0 },
    staleTime: 30_000,
  });
}

export function useInvitesQuery() {
  return useQuery({
    key: () => ["invites"] as const,
    query: () => useInternalApi<Invites>("invites"),
    staleTime: 30_000,
  });
}
