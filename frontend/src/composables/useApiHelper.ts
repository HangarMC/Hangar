import { useApi, useInternalApi } from "~/composables/useApi";
import { PaginatedResult, Project, ProjectCompact, User, Version } from "hangar-api";
import { useInitialState } from "~/composables/useInitialState";
import {
  Flag,
  HangarNotification,
  HangarProject,
  HangarVersion,
  HealthReport,
  Invites,
  LoggedAction,
  Note,
  Organization,
  ProjectChannel,
  ProjectOwner,
  ProjectPage,
  ReviewQueueEntry,
  RoleTable,
} from "hangar-internal";

export async function useProjects(params: Record<string, any> = { limit: 25, offset: 0 }, blocking = true) {
  return useInitialState("useProjects", () => useApi<PaginatedResult<Project>>("projects", false, "get", params), blocking);
}

export async function useUser(user: string, blocking = true) {
  return useInitialState("useUser:" + user, () => useApi<User>("users/" + user, false), blocking);
}

export async function useOrganization(user: string, blocking = true) {
  return useInitialState("useOrganization:" + user, () => useInternalApi<Organization>(`organizations/org/${user}`, false), blocking);
}

export async function useProject(user: string, project: string, blocking = true) {
  return useInitialState(
    "useProject:" + user + ":" + project,
    () => useInternalApi<HangarProject>("projects/project/" + user + "/" + project, false),
    blocking
  );
}

export async function useStargazers(user: string, project: string, blocking = true) {
  return useInitialState(
    "useStargazers:" + user + ":" + project,
    () => useApi<PaginatedResult<User>>(`projects/${user}/${project}/stargazers`, false),
    blocking
  );
}

export async function useWatchers(user: string, project: string, blocking = true) {
  return useInitialState("useWatchers:" + user + ":" + project, () => useApi<PaginatedResult<User>>(`projects/${user}/${project}/watchers`, false), blocking);
}

export async function useStaff(blocking = true) {
  return useInitialState("useStaff", () => useApi<PaginatedResult<User>>("staff", false), blocking);
}

export async function useAuthors(blocking = true) {
  return useInitialState("useAuthors", () => useApi<PaginatedResult<User>>("authors", false), blocking);
}

export async function useInvites(blocking = true) {
  return useInitialState("useInvites", () => useInternalApi<Invites>("invites", false), blocking);
}

export async function useNotifications(blocking = true) {
  return useInitialState("useNotifications", () => useInternalApi<PaginatedResult<HangarNotification>>("notifications", false), blocking);
}

export async function useUnreadNotifications(blocking = true) {
  return useInitialState("useUnreadNotifications", () => useInternalApi<PaginatedResult<HangarNotification>>("unreadnotifications", false), blocking);
}

export async function useReadNotifications(blocking = true) {
  return useInitialState("useReadNotifications", () => useInternalApi<PaginatedResult<HangarNotification>>("readnotifications", false), blocking);
}

export async function useRecentNotifications(blocking = true, amount: number) {
  return useInitialState(
    "useRecentNotifications:" + amount,
    () => useInternalApi<HangarNotification[]>("recentnotifications?amount=" + amount, false),
    blocking
  );
}

export async function useUnreadNotificationsCount(blocking = true) {
  return useInitialState("useUnreadNotificationsCount", () => useInternalApi<number>("unreadcount", false), blocking);
}

export async function useResolvedFlags(blocking = true) {
  return useInitialState("useResolvedFlags", () => useInternalApi<PaginatedResult<Flag>>("flags/resolved", false), blocking);
}

export async function useUnresolvedFlags(blocking = true) {
  return useInitialState("useUnresolvedFlags", () => useInternalApi<PaginatedResult<Flag>>("flags/unresolved", false), blocking);
}

export async function useProjectFlags(projectId: number, blocking = true) {
  return useInitialState("useProjectFlags:" + projectId, () => useInternalApi<Flag[]>("flags/" + projectId, false), blocking);
}

export async function useProjectNotes(projectId: number, blocking = true) {
  return useInitialState("useProjectNotes:" + projectId, () => useInternalApi<Note[]>("projects/notes/" + projectId, false), blocking);
}

export async function useProjectChannels(user: string, project: string, blocking = true) {
  return useInitialState("useProjectChannels:" + user + ":" + project, () => useInternalApi<ProjectChannel[]>(`channels/${user}/${project}`, false), blocking);
}

export async function useProjectVersions(user: string, project: string, blocking = true) {
  return useInitialState(
    "useProjectVersions:" + user + ":" + project,
    () => useApi<PaginatedResult<Version>>(`projects/${user}/${project}/versions`, false),
    blocking
  );
}

export async function useProjectVersionsInternal(user: string, project: string, version: string, blocking = true) {
  return useInitialState(
    "useProjectVersionsInternal:" + user + ":" + project + ":" + version,
    () => useInternalApi<HangarVersion>(`versions/version/${user}/${project}/versions/${version}`, false),
    blocking
  );
}

export async function usePage(user: string, project: string, path?: string, blocking = true) {
  return useInitialState(
    "usePage:" + user + ":" + project + ":" + path,
    () => useInternalApi<ProjectPage>(`pages/page/${user}/${project}` + (path ? "/" + path : ""), false),
    blocking
  );
}

export async function useHealthReport(blocking = true) {
  return useInitialState("useHealthReport", () => useInternalApi<HealthReport>("admin/health", false), blocking);
}

export async function useActionLogs(blocking = true) {
  return useInitialState("useActionLogs", () => useInternalApi<PaginatedResult<LoggedAction>>("admin/log/", false), blocking);
}

export async function useVersionApprovals(blocking = true) {
  return useInitialState(
    "useVersionApprovals",
    () => useInternalApi<{ underReview: ReviewQueueEntry[]; notStarted: ReviewQueueEntry[] }>("admin/approval/versions", false),
    blocking
  );
}

export async function usePossibleOwners(blocking = true) {
  return useInitialState("usePossibleOwners", () => useInternalApi<ProjectOwner[]>("projects/possibleOwners"), blocking);
}

export async function useOrgVisibility(user: string, blocking = true) {
  return useInitialState(
    "useOrgVisibility:" + user,
    () => useInternalApi<{ [key: string]: boolean }>(`organizations/${user}/userOrganizationsVisibility`, true),
    blocking
  );
}

export async function useUserData(user: string, blocking = true) {
  return useInitialState(
    "useUserData:" + user,
    async () => {
      // noinspection ES6MissingAwait
      const data = await Promise.all([
        useApi<PaginatedResult<ProjectCompact>>(`users/${user}/starred`, false),
        useApi<PaginatedResult<ProjectCompact>>(`users/${user}/watching`, false),
        useApi<PaginatedResult<Project>>(`projects`, false, "get", {
          owner: user,
        }),
        useInternalApi<{ [key: string]: RoleTable }>(`organizations/${user}/userOrganizations`, false),
        useApi<ProjectCompact[]>(`users/${user}/pinned`, false),
      ] as Promise<any>[]);
      return {
        starred: data[0] as PaginatedResult<ProjectCompact>,
        watching: data[1] as PaginatedResult<ProjectCompact>,
        projects: data[2] as PaginatedResult<Project>,
        organizations: data[3] as { [key: string]: RoleTable },
        pinned: data[4] as ProjectCompact[],
      };
    },
    blocking
  );
}
