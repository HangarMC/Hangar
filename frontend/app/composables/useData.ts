import type { Router } from "vue-router";
import type {
  ApiKey,
  Invites,
  PaginatedResultHangarNotification,
  PaginatedResultProject,
  PaginatedResultVersion,
  ProjectOwner,
  Platform,
} from "#shared/types/backend";

type LegacyStatus = "idle" | "loading" | "success" | "error";

// Maps Pinia Colada's status/asyncStatus to the legacy status values used by consumers
function mapStatus(queryReturn: { asyncStatus: { value: string }; status: { value: string } }): ComputedRef<LegacyStatus> {
  return computed(() => {
    if (queryReturn.asyncStatus.value === "loading") return "loading";
    if (queryReturn.status.value === "success") return "success";
    if (queryReturn.status.value === "error") return "error";
    return "idle";
  });
}

export function useOrganizationVisibility(user: () => string) {
  const q = useOrganizationVisibilityQuery(user);
  return { organizationVisibility: q.data, organizationVisibilityStatus: mapStatus(q) };
}

export function usePossibleAlts(user: () => string) {
  const q = usePossibleAltsQuery(user);
  return { possibleAlts: q.data, possibleAltsStatus: mapStatus(q) };
}

export function useProjects(
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
  const q = useProjectsQuery(params, router);
  return { projects: q.data as Ref<PaginatedResultProject | undefined>, projectsStatus: mapStatus(q), refreshProjects: () => q.refetch() };
}

export function useStarred(user: () => string) {
  const q = useStarredQuery(user);
  return { starred: q.data, starredStatus: mapStatus(q) };
}

export function useWatching(user: () => string) {
  const q = useWatchingQuery(user);
  return { watching: q.data, watchingStatus: mapStatus(q) };
}

export function usePinned(user: () => string) {
  const q = usePinnedQuery(user);
  return { pinned: q.data, pinnedStatus: mapStatus(q) };
}

export function useOrganizations(user: () => string) {
  const q = useOrganizationsQuery(user);
  return { organizations: q.data, organizationsStatus: mapStatus(q) };
}

export function useVersionInfo() {
  const q = useVersionInfoQuery();
  return { version: q.data, versionStatus: mapStatus(q) };
}

export function useUnreadNotifications() {
  const q = useUnreadNotificationsQuery();
  const queryCache = useQueryCache();
  const unreadNotifications = computed({
    get: () => q.data.value,
    set: (val) => queryCache.setQueryData(["unreadNotifications"], val),
  });
  return { unreadNotifications, unreadNotificationsStatus: mapStatus(q) };
}

export function useReadNotifications() {
  const q = useReadNotificationsQuery();
  return { readNotifications: q.data, readNotificationsStatus: mapStatus(q) };
}

export function useUnreadCount() {
  const q = useUnreadCountQuery();
  // Provide a safe default so consumers don't need to handle undefined
  const unreadCount = computed({
    get: () => q.data.value ?? { notifications: 0, invites: 0 },
    set: (val) => {
      // Allow consumers to update the value optimistically
      const queryCache = useQueryCache();
      queryCache.setQueryData(["unreadCount"], val);
    },
  });
  return { unreadCount, unreadCountStatus: mapStatus(q), refreshUnreadCount: () => q.refetch() };
}

export function useNotifications() {
  const q = useNotificationsQuery();
  const queryCache = useQueryCache();
  const notifications = computed({
    get: () => q.data.value,
    set: (val) => queryCache.setQueryData(["notifications"], val),
  });
  return { notifications, notificationsStatus: mapStatus(q) };
}

export function useInvites() {
  const q = useInvitesQuery();
  const queryCache = useQueryCache();
  const invites = computed({
    get: () => q.data.value,
    set: (val) => queryCache.setQueryData(["invites"], val),
  });
  return { invites, invitesStatus: mapStatus(q) };
}

export function usePossibleOwners() {
  const q = usePossibleOwnersQuery();
  const projectOwners = computed(() => q.data.value ?? ([] as ProjectOwner[]));
  return { projectOwners, projectOwnersStatus: mapStatus(q) };
}

export function useAuthSettings() {
  const q = useAuthSettingsQuery();
  return { authSettings: q.data, authSettingsStatus: mapStatus(q), refreshAuthSettings: () => q.refetch() };
}

export function useApiKeys(user: () => string) {
  const q = useApiKeysQuery(user);
  const queryCache = useQueryCache();
  const apiKeys = computed({
    get: () => q.data.value,
    set: (val) => queryCache.setQueryData(["apiKeys", user()], val),
  });
  return { apiKeys, apiKeysStatus: mapStatus(q) };
}

export function usePossiblePerms(user: () => string) {
  const q = usePossiblePermsQuery(user);
  return { possiblePerms: q.data, possiblePermsStatus: mapStatus(q) };
}

export function useAdminStats(params: () => { from: string; to: string }) {
  const q = useAdminStatsQuery(params);
  return { adminStats: q.data, adminStatsStatus: mapStatus(q) };
}

export function useHealthReport() {
  const q = useHealthReportQuery();
  return { healthReport: q.data, healthReportStatus: mapStatus(q), healthReportRefresh: () => q.refetch() };
}

export function useResolvedFlags() {
  const q = useResolvedFlagsQuery();
  return { flags: q.data, flagsStatus: mapStatus(q) };
}

export function useUnresolvedFlags() {
  const q = useUnresolvedFlagsQuery();
  return { flags: q.data, flagsStatus: mapStatus(q) };
}

export function useVersionApprovals() {
  const q = useVersionApprovalsQuery();
  return { versionApprovals: q.data, versionApprovalsStatus: mapStatus(q) };
}

export function useUser(userName: () => string) {
  const q = useUserQuery(userName);
  return { user: q.data, userStatus: mapStatus(q), refreshUser: () => q.refetch() };
}

export function useUsers(params: () => { query?: string; limit?: number; offset?: number; sort?: string[] }) {
  const q = useUsersQuery(params);
  return { users: q.data, usersStatus: mapStatus(q) };
}

export function useActionLogs(
  params: () => { limit: number; offset: number; sort: string[]; user?: string; logAction?: string; authorName?: string; projectSlug?: string },
  router?: Router
) {
  const q = useActionLogsQuery(params, router);
  return { actionLogs: q.data, actionLogsStatus: mapStatus(q) };
}

export function useStaff(params: () => { offset?: number; limit?: number; sort?: string[]; query?: string }) {
  const q = useStaffQuery(params);
  return { staff: q.data, staffStatus: mapStatus(q) };
}

export function useAuthors(params: () => { offset?: number; limit?: number; sort?: string[]; query?: string }) {
  const q = useAuthorsQuery(params);
  return { authors: q.data, authorStatus: mapStatus(q) };
}

export function useWatchers(project: () => string) {
  const q = useWatchersQuery(project);
  return { watchers: q.data, watchersStatus: mapStatus(q) };
}

export function useStargazers(project: () => string) {
  const q = useStargazersQuery(project);
  return { stargazers: q.data, stargazersStatus: mapStatus(q) };
}

export function useProjectChannels(project: () => string) {
  const q = useProjectChannelsQuery(project);
  return {
    channels: q.data,
    channelsStatus: mapStatus(q),
    refreshChannels: () => q.refetch(),
    channelPromise: undefined as Promise<void> | undefined,
  };
}

export function useProjectNotes(project: () => string) {
  const q = useProjectNotesQuery(project);
  return { notes: q.data, notesStatus: mapStatus(q), refreshNotes: () => q.refetch() };
}

export function useProjectFlags(project: () => string) {
  const q = useProjectFlagsQuery(project);
  return { flags: q.data, flagsStatus: mapStatus(q) };
}

export function useProjectVersions(
  params: () => { project: string; data: { limit: number; offset: number; channel: string[]; platform: Platform[]; includeHiddenChannels: boolean } },
  router: Router
) {
  const q = useProjectVersionsQuery(params, router);
  return { versions: q.data as Ref<PaginatedResultVersion | undefined>, versionsStatus: mapStatus(q) };
}

export function usePage(params: () => { project: string; path?: string }) {
  const q = usePageQuery(params);
  return { page: q.data, pageStatus: mapStatus(q) };
}

export function useReviews(version: () => string) {
  const q = useReviewsQuery(version);
  return { reviews: q.data, reviewsStatus: mapStatus(q), refreshReviews: () => q.refetch() };
}

export function useJarScans(version: () => string) {
  const q = useJarScansQuery(version);
  return { jarScans: q.data, jarScansStatus: mapStatus(q), refreshJarScans: () => q.refetch() };
}
