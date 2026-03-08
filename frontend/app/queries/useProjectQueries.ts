import type { Router } from "vue-router";
import type {
  HangarChannel,
  HangarProjectFlag,
  HangarProjectNote,
  PaginatedResultProject,
  PaginatedResultProjectCompact,
  PaginatedResultVersion,
  Platform,
  ProjectCompact,
  ProjectPageTable,
} from "#shared/types/backend";

export function useProjectsQuery(
  params: () => {
    member?: string;
    limit?: number;
    offset?: number;
    query?: string;
    owner?: string;
    version?: string[];
    category?: string[];
    platform?: Platform[];
    tag?: string[];
    sort?: string;
  },
  router?: Router
) {
  const p = params();
  return useQuery({
    key: () => ["projects", params().member || params().owner || "main", params().offset] as const,
    query: () => useApi<PaginatedResultProject>("projects", "get", { ...params() }),
    staleTime: 30_000,
  });
}

export function useStarredQuery(user: () => string) {
  return useQuery({
    key: () => ["starred", user()] as const,
    query: () => useApi<PaginatedResultProjectCompact>(`users/${user()}/starred`),
    staleTime: 60_000,
  });
}

export function useWatchingQuery(user: () => string) {
  return useQuery({
    key: () => ["watching", user()] as const,
    query: () => useApi<PaginatedResultProjectCompact>(`users/${user()}/watching`),
    staleTime: 60_000,
  });
}

export function usePinnedQuery(user: () => string) {
  return useQuery({
    key: () => ["pinned", user()] as const,
    query: () => useApi<ProjectCompact[]>(`users/${user()}/pinned`),
    staleTime: 60_000,
  });
}

export function useProjectChannelsQuery(project: () => string) {
  return useQuery({
    key: () => ["channels", project()] as const,
    query: () => useInternalApi<(HangarChannel & { temp?: boolean })[]>(`channels/${project()}`),
    staleTime: 60_000,
  });
}

export function useProjectNotesQuery(project: () => string) {
  return useQuery({
    key: () => ["notes", project()] as const,
    query: () => useInternalApi<HangarProjectNote[]>("projects/notes/" + project()),
    staleTime: 30_000,
  });
}

export function useProjectFlagsQuery(project: () => string) {
  return useQuery({
    key: () => ["flags", project()] as const,
    query: () => useInternalApi<HangarProjectFlag[]>("flags/" + project()),
    staleTime: 30_000,
  });
}

export function useProjectVersionsQuery(
  params: () => { project: string; data: { limit: number; offset: number; channel: string[]; platform: Platform[]; includeHiddenChannels: boolean } },
  router: Router
) {
  const p = params();
  return useQuery({
    key: () =>
      ["versions", params().project, params().data.offset, params().data.channel, params().data.platform, params().data.includeHiddenChannels] as const,
    query: () => useApi<PaginatedResultVersion>(`projects/${params().project}/versions`, "GET", params().data),
    staleTime: 30_000,
  });
}

export function usePageQuery(params: () => { project: string; path?: string }) {
  return useQuery({
    key: () => ["page", params().project, params().path] as const,
    query: () => useInternalApi<ProjectPageTable>(`pages/page/${params().project}` + (params().path ? "/" + params().path.replaceAll(",", "/") : "")),
    staleTime: 60_000,
  });
}

export function useWatchersQuery(project: () => string) {
  return useQuery({
    key: () => ["watchers", project()] as const,
    query: () => useApi<import("#shared/types/backend").PaginatedResultUser>(`projects/${project()}/watchers`),
    staleTime: 60_000,
  });
}

export function useStargazersQuery(project: () => string) {
  return useQuery({
    key: () => ["stargazers", project()] as const,
    query: () => useApi<import("#shared/types/backend").PaginatedResultUser>(`projects/${project()}/stargazers`),
    staleTime: 60_000,
  });
}
