import type {
  PaginatedResultUser,
  OrganizationRoleTable,
  User,
} from "#shared/types/backend";
import { NamedPermission } from "#shared/types/backend";

export function useOrganizationVisibilityQuery(user: () => string) {
  return useQuery({
    key: () => ["organizationVisibility", user()] as const,
    query: () => useInternalApi<{ [key: string]: boolean }>(`organizations/${user()}/userOrganizationsVisibility`),
    enabled: () => user() === useAuthStore().user?.name,
    staleTime: 60_000,
  });
}

export function usePossibleAltsQuery(user: () => string) {
  return useQuery({
    key: () => ["possibleAlts", user()] as const,
    query: () => useInternalApi<string[]>(`users/${user()}/alts`),
    enabled: () => hasPerms(NamedPermission.IsStaff),
    staleTime: 60_000,
  });
}

export function useOrganizationsQuery(user: () => string) {
  return useQuery({
    key: () => ["organizations", user()] as const,
    query: () => useInternalApi<{ [key: string]: OrganizationRoleTable }>(`organizations/${user()}/userOrganizations`),
    staleTime: 60_000,
  });
}

export function useUserQuery(userName: () => string) {
  return useQuery({
    key: () => ["user", userName()] as const,
    query: () => useApi<User>("users/" + userName()),
    staleTime: 60_000,
  });
}

export function useUsersQuery(params: () => { query?: string; limit?: number; offset?: number; sort?: string[] }) {
  return useQuery({
    key: () => ["users", params().query, params().offset, params().sort] as const,
    query: () => useApi<PaginatedResultUser>("users", "get", params()),
    staleTime: 30_000,
  });
}

export function useStaffQuery(params: () => { offset?: number; limit?: number; sort?: string[]; query?: string }) {
  return useQuery({
    key: () => ["staff", params().offset, params().sort, params().query] as const,
    query: () => useApi<PaginatedResultUser>("staff", "GET", params()),
    staleTime: 60_000,
  });
}

export function useAuthorsQuery(params: () => { offset?: number; limit?: number; sort?: string[]; query?: string }) {
  return useQuery({
    key: () => ["authors", params().offset, params().sort, params().query] as const,
    query: () => useApi<PaginatedResultUser>("authors", "GET", params()),
    staleTime: 60_000,
  });
}
