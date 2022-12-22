import { PaginatedResult, Project, ProjectCompact, User, Version } from "hangar-api";
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
import { useApi, useInternalApi } from "~/composables/useApi";
import { useAsyncData } from "#imports";

export async function useProjects(params: Record<string, any> = { limit: 25, offset: 0 }) {
  return (await useAsyncData("useProjects", () => useApi<PaginatedResult<Project>>("projects", "get", params))).data;
}

export async function useUser(user: string) {
  return (await useAsyncData("useUser:" + user, () => useApi<User>("users/" + user))).data;
}

export async function useOrganization(user: string) {
  return (await useAsyncData("useOrganization:" + user, () => useInternalApi<Organization>(`organizations/org/${user}`))).data;
}

export async function useProject(user: string, project: string) {
  return (await useAsyncData("useProject:" + user + ":" + project, () => useInternalApi<HangarProject>("projects/project/" + user + "/" + project))).data;
}

export async function useStargazers(user: string, project: string) {
  return (await useAsyncData("useStargazers:" + user + ":" + project, () => useApi<PaginatedResult<User>>(`projects/${user}/${project}/stargazers`))).data;
}

export async function useWatchers(user: string, project: string) {
  return (await useAsyncData("useWatchers:" + user + ":" + project, () => useApi<PaginatedResult<User>>(`projects/${user}/${project}/watchers`))).data;
}

export async function useStaff() {
  return (await useAsyncData("useStaff", () => useApi<PaginatedResult<User>>("staff"))).data;
}

export async function useAuthors() {
  return (await useAsyncData("useAuthors", () => useApi<PaginatedResult<User>>("authors"))).data;
}

export async function useUsers() {
  return (await useAsyncData("useUsers", () => useApi<PaginatedResult<User>>("users"))).data;
}

export async function useInvites() {
  return (await useAsyncData("useInvites", () => useInternalApi<Invites>("invites"))).data;
}

export async function useNotifications() {
  return (await useAsyncData("useNotifications", () => useInternalApi<PaginatedResult<HangarNotification>>("notifications"))).data;
}

export async function useUnreadNotifications() {
  return (await useAsyncData("useUnreadNotifications", () => useInternalApi<PaginatedResult<HangarNotification>>("unreadnotifications"))).data;
}

export async function useReadNotifications() {
  return (await useAsyncData("useReadNotifications", () => useInternalApi<PaginatedResult<HangarNotification>>("readnotifications"))).data;
}

export async function useRecentNotifications(amount: number) {
  return (await useAsyncData("useRecentNotifications:" + amount, () => useInternalApi<HangarNotification[]>("recentnotifications?amount=" + amount))).data;
}

export async function useUnreadNotificationsCount() {
  return (await useAsyncData("useUnreadNotificationsCount", () => useInternalApi<number>("unreadcount"))).data;
}

export async function useResolvedFlags() {
  return (await useAsyncData("useResolvedFlags", () => useInternalApi<PaginatedResult<Flag>>("flags/resolved"))).data;
}

export async function useUnresolvedFlags() {
  return (await useAsyncData("useUnresolvedFlags", () => useInternalApi<PaginatedResult<Flag>>("flags/unresolved"))).data;
}

export async function useProjectFlags(projectId: number) {
  return (await useAsyncData("useProjectFlags:" + projectId, () => useInternalApi<Flag[]>("flags/" + projectId))).data;
}

export async function useProjectNotes(projectId: number) {
  return (await useAsyncData("useProjectNotes:" + projectId, () => useInternalApi<Note[]>("projects/notes/" + projectId))).data;
}

export async function useProjectChannels(user: string, project: string) {
  return (await useAsyncData("useProjectChannels:" + user + ":" + project, () => useInternalApi<ProjectChannel[]>(`channels/${user}/${project}`))).data;
}

export async function useProjectVersions(user: string, project: string) {
  return (await useAsyncData("useProjectVersions:" + user + ":" + project, () => useApi<PaginatedResult<Version>>(`projects/${user}/${project}/versions`)))
    .data;
}

export async function useProjectVersionsInternal(user: string, project: string, version: string) {
  return (
    await useAsyncData("useProjectVersionsInternal:" + user + ":" + project + ":" + version, () =>
      useInternalApi<HangarVersion>(`versions/version/${user}/${project}/versions/${version}`)
    )
  ).data;
}

export async function usePage(user: string, project: string, path?: string) {
  return (
    await useAsyncData("usePage:" + user + ":" + project + ":" + path, () =>
      useInternalApi<ProjectPage>(`pages/page/${user}/${project}` + (path ? "/" + path : ""))
    )
  ).data;
}

export async function useHealthReport() {
  return (await useAsyncData("useHealthReport", () => useInternalApi<HealthReport>("admin/health"))).data;
}

export async function useActionLogs() {
  return (await useAsyncData("useActionLogs", () => useInternalApi<PaginatedResult<LoggedAction>>("admin/log/"))).data;
}

export async function useVersionApprovals() {
  return (
    await useAsyncData("useVersionApprovals", () =>
      useInternalApi<{ underReview: ReviewQueueEntry[]; notStarted: ReviewQueueEntry[] }>("admin/approval/versions")
    )
  ).data;
}

export async function usePossibleOwners() {
  return (await useAsyncData("usePossibleOwners", () => useInternalApi<ProjectOwner[]>("projects/possibleOwners"))).data;
}

export async function useOrgVisibility(user: string) {
  return (await useAsyncData("useOrgVisibility:" + user, () => useInternalApi<{ [key: string]: boolean }>(`organizations/${user}/userOrganizationsVisibility`)))
    .data;
}

export async function useUserData(user: string) {
  return (
    await useAsyncData("useUserData:" + user, async () => {
      // noinspection ES6MissingAwait
      const data = await Promise.all([
        useApi<PaginatedResult<ProjectCompact>>(`users/${user}/starred`),
        useApi<PaginatedResult<ProjectCompact>>(`users/${user}/watching`),
        useApi<PaginatedResult<Project>>(`projects`, "get", {
          owner: user,
        }),
        useInternalApi<{ [key: string]: RoleTable }>(`organizations/${user}/userOrganizations`),
        useApi<ProjectCompact[]>(`users/${user}/pinned`),
      ]);
      return {
        starred: data[0] as PaginatedResult<ProjectCompact>,
        watching: data[1] as PaginatedResult<ProjectCompact>,
        projects: data[2] as PaginatedResult<Project>,
        organizations: data[3] as { [key: string]: RoleTable },
        pinned: data[4] as ProjectCompact[],
      };
    })
  ).data;
}
