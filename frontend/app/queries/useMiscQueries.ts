import { NamedPermission } from "#shared/types/backend";
import type {
  ApiKey,
  HangarReview,
  JarScanResult,
  ProjectOwner,
  SettingsResponse,
  VersionInfo,
} from "#shared/types/backend";

export function useVersionInfoQuery() {
  return useQuery({
    key: () => ["versionInfo"] as const,
    query: () => useInternalApi<VersionInfo>(`data/version-info`),
    staleTime: 300_000,
  });
}

export function usePossibleOwnersQuery() {
  return useQuery({
    key: () => ["possibleOwners"] as const,
    query: () => useInternalApi<ProjectOwner[]>("projects/possibleOwners"),
    placeholderData: () => [] as ProjectOwner[],
    staleTime: 60_000,
  });
}

export function useAuthSettingsQuery() {
  return useQuery({
    key: () => ["authSettings"] as const,
    query: () => useInternalApi<SettingsResponse>(`auth/settings`, "POST"),
    staleTime: 30_000,
  });
}

export function useApiKeysQuery(user: () => string) {
  return useQuery({
    key: () => ["apiKeys", user()] as const,
    query: () => useInternalApi<ApiKey[]>("api-keys/existing-keys/" + user()),
    staleTime: 30_000,
  });
}

export function usePossiblePermsQuery(user: () => string) {
  return useQuery({
    key: () => ["possiblePerms", user()] as const,
    query: () => useInternalApi<NamedPermission[]>("api-keys/possible-perms/" + user()),
    staleTime: 60_000,
  });
}

export function useReviewsQuery(version: () => string) {
  return useQuery({
    key: () => ["reviews", version()] as const,
    query: () => useInternalApi<HangarReview[]>(`reviews/${version()}/reviews`),
    staleTime: 30_000,
  });
}

export function useJarScansQuery(version: () => string) {
  return useQuery({
    key: () => ["jarScans", version()] as const,
    query: () => useInternalApi<JarScanResult[]>(`jarscanning/result/${version()}`),
    staleTime: 30_000,
  });
}
