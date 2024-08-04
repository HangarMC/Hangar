import {
  type ApiKey,
  type DayStats,
  type ExtendedProjectPage,
  type HangarChannel,
  type HangarProjectFlag,
  type HangarProjectNote,
  type HealthReport,
  type Invites,
  NamedPermission,
  type OrganizationRoleTable,
  type PaginatedResultHangarLoggedAction,
  type PaginatedResultHangarNotification,
  type PaginatedResultHangarProjectFlag,
  type PaginatedResultProject,
  type PaginatedResultProjectCompact,
  type PaginatedResultUser,
  type PaginatedResultVersion,
  type ProjectCompact,
  type ProjectOwner,
  type ReviewQueue,
  type SettingsResponse,
  type User,
  type VersionInfo,
} from "~/types/backend";
import type { Router } from "vue-router";

export function useOrganizationVisibility(user: () => string) {
  const { data: organizationVisibility, status: organizationVisibilityStatus } = useData(
    user,
    (u) => "organizationVisibility:" + u,
    (u) => useInternalApi<{ [key: string]: boolean }>(`organizations/${u}/userOrganizationsVisibility`),
    false,
    (u) => u !== useAuthStore().user?.name
  );
  return { organizationVisibility, organizationVisibilityStatus };
}

export function usePossibleAlts(user: () => string) {
  const { data: possibleAlts, status: possibleAltsStatus } = useData(
    user,
    (u) => "possibleAlts:" + u,
    (u) => useInternalApi<string[]>(`users/${u}/alts`),
    false,
    () => !hasPerms(NamedPermission.IsStaff)
  );
  return { possibleAlts, possibleAltsStatus };
}

export function useProjects(params: () => { member?: string; limit?: number; offset?: number; q?: string; owner?: string }, router?: Router) {
  const { data: projects, status: projectsStatus } = useData(
    params,
    (p) => "projects:" + p,
    (p) => useApi<PaginatedResultProject>("projects", "get", { ...p }),
    true,
    () => false,
    ({ offset, limit, member, ...paramsWithoutLimit }) => {
      if (router) {
        router.replace({ query: { page: offset && limit ? Math.floor(offset / limit) : undefined, ...paramsWithoutLimit } });
      }
    }
  );
  return { projects, projectsStatus };
}

export function useStarred(user: () => string) {
  const { data: starred, status: starredStatus } = useData(
    user,
    (u) => "starred:" + u,
    (u) => useApi<PaginatedResultProjectCompact>(`users/${u}/starred`)
  );
  return { starred, starredStatus };
}

export function useWatching(user: () => string) {
  const { data: watching, status: watchingStatus } = useData(
    user,
    (u) => "watching:" + u,
    (u) => useApi<PaginatedResultProjectCompact>(`users/${u}/watching`)
  );
  return { watching, watchingStatus };
}

export function usePinned(user: () => string) {
  const { data: pinned, status: pinnedStatus } = useData(
    user,
    (u) => "pinned:" + u,
    (u) => useApi<ProjectCompact[]>(`users/${u}/pinned`)
  );
  return { pinned, pinnedStatus };
}

export function useOrganizations(user: () => string) {
  const { data: organizations, status: organizationsStatus } = useData(
    user,
    (u) => "organizations:" + u,
    (u) => useInternalApi<{ [key: string]: OrganizationRoleTable }>(`organizations/${u}/userOrganizations`)
  );
  return { organizations, organizationsStatus };
}

export function useVersionInfo() {
  const { data: version, status: versionStatus } = useData(
    () => ({}),
    () => "versionInfo",
    () => useInternalApi<VersionInfo>(`data/version-info`)
  );
  return { version, versionStatus };
}

export function useUnreadNotifications() {
  const { data: unreadNotifications, status: unreadNotificationsStatus } = useData(
    () => ({}),
    () => "unreadNotifications",
    () => useInternalApi<PaginatedResultHangarNotification>("unreadnotifications")
  );
  return { unreadNotifications, unreadNotificationsStatus };
}

export function useReadNotifications() {
  const { data: readNotifications, status: readNotificationsStatus } = useData(
    () => ({}),
    () => "readNotifications",
    () => useInternalApi<PaginatedResultHangarNotification>("readnotifications")
  );
  return { readNotifications, readNotificationsStatus };
}

export function useNotifications() {
  const { data: notifications, status: notificationsStatus } = useData(
    () => ({}),
    () => "notifications",
    () => useInternalApi<PaginatedResultHangarNotification>("notifications")
  );
  return { notifications, notificationsStatus };
}

export function useInvites() {
  const { data: invites, status: invitesStatus } = useData(
    () => ({}),
    () => "invites",
    () => useInternalApi<Invites>("invites")
  );
  return { invites, invitesStatus };
}

export function usePossibleOwners() {
  const { data: projectOwners, status: projectOwnersStatus } = useData(
    () => ({}),
    () => "possibleOwners",
    () => useInternalApi<ProjectOwner[]>("projects/possibleOwners"),
    true,
    () => false,
    () => {},
    []
  );
  // TODO a default value should change the type so that this cast isnt needed
  return { projectOwners: projectOwners as Ref<ProjectOwner[]>, projectOwnersStatus };
}

export function useAuthSettings() {
  const {
    data: authSettings,
    status: authSettingsStatus,
    refresh: refreshAuthSettings,
  } = useData(
    () => ({}),
    () => "authSettings",
    () => useInternalApi<SettingsResponse>(`auth/settings`, "POST")
  );
  return { authSettings, authSettingsStatus, refreshAuthSettings };
}

export function useApiKeys(user: () => string) {
  const { data: apiKeys, status: apiKeysStatus } = useData(
    user,
    (u) => "apiKeys:" + u,
    (u) => useInternalApi<ApiKey[]>("api-keys/existing-keys/" + u)
  );
  return { apiKeys, apiKeysStatus };
}

export function usePossiblePerms(user: () => string) {
  const { data: possiblePerms, status: possiblePermsStatus } = useData(
    user,
    (u) => "possiblePerms:" + u,
    (u) => useInternalApi<NamedPermission[]>("api-keys/possible-perms/" + u)
  );
  return { possiblePerms, possiblePermsStatus };
}

export function useAdminStats(params: () => { from: string; to: string }) {
  const { data: adminStats, status: adminStatsStatus } = useData(
    params,
    () => "adminStats:" + params().from + params().to,
    (params) => useInternalApi<DayStats[]>("admin/stats", "get", params)
  );
  return { adminStats, adminStatsStatus };
}

export function useHealthReport() {
  const { data: healthReport, status: healthReportStatus } = useData(
    () => ({}),
    () => "healthReport",
    () => useInternalApi<HealthReport>("admin/health", "GET", undefined, { timeout: 60_000 })
  );
  return { healthReport, healthReportStatus };
}

export function useResolvedFlags() {
  const { data: flags, status: flagsStatus } = useData(
    () => ({}),
    () => "resolvedFlags",
    () => useInternalApi<PaginatedResultHangarProjectFlag>("flags/resolved")
  );
  return { flags, flagsStatus };
}

export function useUnresolvedFlags() {
  const { data: flags, status: flagsStatus } = useData(
    () => ({}),
    () => "unresolvedFlags",
    () => useInternalApi<PaginatedResultHangarProjectFlag>("flags/unresolved")
  );
  return { flags, flagsStatus };
}

export function useVersionApprovals() {
  const { data: versionApprovals, status: versionApprovalsStatus } = useData(
    () => ({}),
    () => "versionApprovals",
    () => useInternalApi<ReviewQueue>("admin/approval/versions")
  );
  return { versionApprovals, versionApprovalsStatus };
}

export function useUser(userName: () => string) {
  const {
    data: user,
    status: userStatus,
    refresh: refreshUser,
  } = useData(
    userName,
    (u) => "user:" + u,
    (u) => useApi<User>("users/" + u)
  );
  return { user, userStatus, refreshUser };
}

export function useUsers(params: () => { query?: string; limit?: number; offset?: number; sort?: string[] }) {
  const { data: users, status: usersStatus } = useData(
    params,
    () => "users",
    (p) => useApi<PaginatedResultUser>("users", "get", p)
  );
  return { users, usersStatus };
}

export function useActionLogs(
  params: () => { limit: number; offset: number; sort: string[]; user?: string; logAction?: string; authorName?: string; projectSlug?: string },
  router?: Router
) {
  const { data: actionLogs, status: actionLogsStatus } = useData(
    params,
    () => "actionLogs",
    (p) => useInternalApi<PaginatedResultHangarLoggedAction>("admin/log", "get", p),
    true,
    () => false,
    ({ offset, limit, ...paramsWithoutLimit }) => {
      if (router) {
        router.replace({ query: { ...paramsWithoutLimit } });
      }
    }
  );
  return { actionLogs, actionLogsStatus };
}

export function useStaff(params: () => { offset?: number; limit?: number; sort?: string[]; query?: string }) {
  const { data: staff, status: staffStatus } = useData(
    params,
    () => "staff",
    (p) => useApi<PaginatedResultUser>("staff", "GET", p)
  );
  return { staff, staffStatus };
}

export function useAuthors(params: () => { offset?: number; limit?: number; sort?: string[]; query?: string }) {
  const { data: authors, status: authorStatus } = useData(
    params,
    () => "authors",
    (p) => useApi<PaginatedResultUser>("authors", "GET", p)
  );
  return { authors, authorStatus };
}

export function useWatchers(project: () => string) {
  const { data: watchers, status: watchersStatus } = useData(
    project,
    (p) => "watchers:" + p,
    (p) => useApi<PaginatedResultUser>(`projects/${p}/watchers`)
  );
  return { watchers, watchersStatus };
}

export function useStargazers(project: () => string) {
  const { data: stargazers, status: stargazersStatus } = useData(
    project,
    (p) => "stargazers:" + p,
    (p) => useApi<PaginatedResultUser>(`projects/${p}/stargazers`)
  );
  return { stargazers, stargazersStatus };
}

export function useProjectChannels(project: () => string) {
  const {
    data: channels,
    status: channelsStatus,
    refresh: refreshChannels,
  } = useData(
    project,
    (p) => "channels:" + p,
    (p) => useInternalApi<(HangarChannel & { temp?: boolean })[]>(`channels/${p}`)
  );
  return { channels, channelsStatus, refreshChannels };
}

export function useProjectNotes(project: () => string) {
  const {
    data: notes,
    status: notesStatus,
    refresh: refreshNotes,
  } = useData(
    project,
    (p) => "notes:" + p,
    (p) => useInternalApi<HangarProjectNote[]>("projects/notes/" + p)
  );
  return { notes, notesStatus, refreshNotes };
}

export function useProjectFlags(project: () => string) {
  const { data: flags, status: flagsStatus } = useData(
    project,
    (p) => "flags:" + p,
    (p) => useInternalApi<HangarProjectFlag[]>("flags/" + p)
  );
  return { flags, flagsStatus };
}

export function useProjectVersions(params: () => { project: string; data: object }) {
  const { data: versions, status: versionsStatus } = useData(
    params,
    (p) => "versions:" + p.project,
    (p) => useApi<PaginatedResultVersion>(`projects/${p.project}/versions`, "GET", p.data)
  );
  return { versions, versionsStatus };
}

export function usePage(params: () => { project: string; path?: string }) {
  const { data: page, status: pageStatus } = useData(
    params,
    (p) => "page:" + p.project + ":" + p.path,
    (p) => useInternalApi<ExtendedProjectPage>(`pages/page/${p.project}` + (p.path ? "/" + p.path.replaceAll(",", "/") : ""))
  );
  return { page, pageStatus };
}
