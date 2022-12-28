import { PaginatedResult, Project, ProjectCompact, User, Version, VersionInfo } from "hangar-api";
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
import { AsyncData } from "nuxt/app";
import { useApi, useInternalApi } from "~/composables/useApi";
import { useAsyncData } from "#imports";

export async function useProjects(params: Record<string, any> = { limit: 25, offset: 0 }) {
  return extract(await useAsyncData("useProjects", () => useApi<PaginatedResult<Project>>("projects", "get", params)));
}

export async function useUser(user: string) {
  return extract(await useAsyncData("useUser:" + user, () => useApi<User>("users/" + user)));
}

export async function useOrganization(user: string) {
  return extract(await useAsyncData("useOrganization:" + user, () => useInternalApi<Organization>(`organizations/org/${user}`)));
}

export async function useProject(user: string, project: string) {
  return extract(await useAsyncData("useProject:" + user + ":" + project, () => useInternalApi<HangarProject>("projects/project/" + user + "/" + project)));
}

export async function useStargazers(user: string, project: string) {
  return extract(await useAsyncData("useStargazers:" + user + ":" + project, () => useApi<PaginatedResult<User>>(`projects/${user}/${project}/stargazers`)));
}

export async function useWatchers(user: string, project: string) {
  return extract(await useAsyncData("useWatchers:" + user + ":" + project, () => useApi<PaginatedResult<User>>(`projects/${user}/${project}/watchers`)));
}

export async function useStaff(params?: { offset?: number; limit?: number; sort?: string[] }) {
  return extract(await useAsyncData("useStaff", () => useApi<PaginatedResult<User>>("staff", "GET", params)));
}

export async function useAuthors(params?: { offset?: number; limit?: number; sort?: string[] }) {
  return extract(await useAsyncData("useAuthors", () => useApi<PaginatedResult<User>>("authors", "GET", params)));
}

export async function useUsers() {
  return extract(await useAsyncData("useUsers", () => useApi<PaginatedResult<User>>("users")));
}

export async function useInvites() {
  return extract(await useAsyncData("useInvites", () => useInternalApi<Invites>("invites")));
}

export async function useNotifications() {
  return extract(await useAsyncData("useNotifications", () => useInternalApi<PaginatedResult<HangarNotification>>("notifications")));
}

export async function useUnreadNotifications() {
  return extract(await useAsyncData("useUnreadNotifications", () => useInternalApi<PaginatedResult<HangarNotification>>("unreadnotifications")));
}

export async function useReadNotifications() {
  return extract(await useAsyncData("useReadNotifications", () => useInternalApi<PaginatedResult<HangarNotification>>("readnotifications")));
}

export async function useRecentNotifications(amount: number) {
  return extract(await useAsyncData("useRecentNotifications:" + amount, () => useInternalApi<HangarNotification[]>("recentnotifications?amount=" + amount)));
}

export async function useUnreadNotificationsCount() {
  return extract(await useAsyncData("useUnreadNotificationsCount", () => useInternalApi<number>("unreadcount")));
}

export async function useResolvedFlags() {
  return extract(await useAsyncData("useResolvedFlags", () => useInternalApi<PaginatedResult<Flag>>("flags/resolved")));
}

export async function useUnresolvedFlags() {
  return extract(await useAsyncData("useUnresolvedFlags", () => useInternalApi<PaginatedResult<Flag>>("flags/unresolved")));
}

export async function useProjectFlags(projectId: number) {
  return extract(await useAsyncData("useProjectFlags:" + projectId, () => useInternalApi<Flag[]>("flags/" + projectId)));
}

export async function useProjectNotes(projectId: number) {
  return extract(await useAsyncData("useProjectNotes:" + projectId, () => useInternalApi<Note[]>("projects/notes/" + projectId)));
}

export async function useProjectChannels(user: string, project: string) {
  return extract(await useAsyncData("useProjectChannels:" + user + ":" + project, () => useInternalApi<ProjectChannel[]>(`channels/${user}/${project}`)));
}

export async function useProjectVersions(user: string, project: string) {
  return extract(
    await useAsyncData("useProjectVersions:" + user + ":" + project, () => useApi<PaginatedResult<Version>>(`projects/${user}/${project}/versions`))
  );
}

export async function useProjectVersionsInternal(user: string, project: string, version: string) {
  return await useAsyncData("useProjectVersionsInternal:" + user + ":" + project + ":" + version, () =>
    useInternalApi<HangarVersion>(`versions/version/${user}/${project}/versions/${version}`)
  );
}

export async function usePage(user: string, project: string, path?: string) {
  return await useAsyncData("usePage:" + user + ":" + project + ":" + path, () =>
    useInternalApi<ProjectPage>(`pages/page/${user}/${project}` + (path ? "/" + path : ""))
  );
}

export async function useHealthReport() {
  return extract(await useAsyncData("useHealthReport", () => useInternalApi<HealthReport>("admin/health")));
}

export async function useActionLogs() {
  return extract(await useAsyncData("useActionLogs", () => useInternalApi<PaginatedResult<LoggedAction>>("admin/log/")));
}

export async function useVersionApprovals() {
  return await useAsyncData("useVersionApprovals", () =>
    useInternalApi<{ underReview: ReviewQueueEntry[]; notStarted: ReviewQueueEntry[] }>("admin/approval/versions")
  );
}

export async function usePossibleOwners() {
  return extract(await useAsyncData("usePossibleOwners", () => useInternalApi<ProjectOwner[]>("projects/possibleOwners")));
}

export async function useOrgVisibility(user: string) {
  return extract(
    await useAsyncData("useOrgVisibility:" + user, () => useInternalApi<{ [key: string]: boolean }>(`organizations/${user}/userOrganizationsVisibility`))
  );
}

export async function useVersionInfo() {
  return extract(await useAsyncData("useVersionInfo", () => useInternalApi<VersionInfo>(`data/version-info`)));
}

export async function useUserData(user: string) {
  return await useAsyncData("useUserData:" + user, async () => {
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
  });
}

function extract<T, E>(asyncData: AsyncData<T, E>) {
  if (asyncData.error?.value) {
    throw asyncData.error.value;
  } else {
    return asyncData.data;
  }
}
