import type {
  FinishedOrPendingHealthReport,
  DayStats,
  PaginatedResultHangarLoggedAction,
  PaginatedResultHangarProjectFlag,
  ReviewQueue,
} from "#shared/types/backend";

export function useAdminStatsQuery(params: () => { from: string; to: string }) {
  return useQuery({
    key: () => ["adminStats", params().from, params().to] as const,
    query: () => useInternalApi<DayStats[]>("admin/stats", "get", params()),
    staleTime: 60_000,
  });
}

export function useHealthReportQuery() {
  return useQuery({
    key: () => ["healthReport"] as const,
    query: () => useInternalApi<FinishedOrPendingHealthReport>("health/", "GET"),
    staleTime: 10_000,
  });
}

export function useResolvedFlagsQuery() {
  return useQuery({
    key: () => ["resolvedFlags"] as const,
    query: () => useInternalApi<PaginatedResultHangarProjectFlag>("flags/resolved"),
    staleTime: 30_000,
  });
}

export function useUnresolvedFlagsQuery() {
  return useQuery({
    key: () => ["unresolvedFlags"] as const,
    query: () => useInternalApi<PaginatedResultHangarProjectFlag>("flags/unresolved"),
    staleTime: 30_000,
  });
}

export function useVersionApprovalsQuery() {
  return useQuery({
    key: () => ["versionApprovals"] as const,
    query: () => useInternalApi<ReviewQueue>("admin/approval/versions"),
    staleTime: 30_000,
  });
}

export function useActionLogsQuery(
  params: () => { limit: number; offset: number; sort: string[]; user?: string; logAction?: string; authorName?: string; projectSlug?: string }
) {
  return useQuery({
    key: () =>
      [
        "actionLogs",
        params().offset,
        params().sort,
        params().user ?? "",
        params().logAction ?? "",
        params().authorName ?? "",
        params().projectSlug ?? "",
      ] as const,
    query: () => useInternalApi<PaginatedResultHangarLoggedAction>("admin/log", "get", params()),
    staleTime: 30_000,
  });
}
