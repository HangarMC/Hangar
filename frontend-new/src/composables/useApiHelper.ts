import { useApi, useInternalApi } from "~/composables/useApi";
import { PaginatedResult, Project, User } from "hangar-api";
import { useInitialState } from "~/composables/useInitialState";
import { Flag, HangarNotification, HangarProject, HealthReport, Invites, LoggedAction, Note, ProjectChannel, ReviewQueueEntry } from "hangar-internal";

export async function useProjects(pagination = { limit: 25, offset: 0 }, blocking = true) {
  return useInitialState("useProjects", () => useApi<PaginatedResult<Project>>("projects", false, "get", pagination), blocking);
}

export async function useUser(user: string, blocking = true) {
  return useInitialState("useUser", () => useApi<User>("users/" + user, false), blocking);
}

export async function useProject(user: string, project: string, blocking = true) {
  return useInitialState("useProject", () => useInternalApi<HangarProject>("projects/project/" + user + "/" + project, false), blocking);
}

export async function useStargazers(user: string, project: string, blocking = true) {
  return useInitialState("useStargazers", () => useApi<PaginatedResult<User>>(`projects/${user}/${project}/stargazers`, false), blocking);
}

export async function useWatchers(user: string, project: string, blocking = true) {
  return useInitialState("useWatchers", () => useApi<PaginatedResult<User>>(`projects/${user}/${project}/watchers`, false), blocking);
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
  return useInitialState("useNotifications", () => useInternalApi<HangarNotification[]>("notifications", false), blocking);
}

export async function useFlags(blocking = true) {
  return useInitialState("useFlags", () => useInternalApi<Flag[]>("flags/", false), blocking);
}

export async function useProjectFlags(projectId: number, blocking = true) {
  return useInitialState("useProjectFlags", () => useInternalApi<Flag[]>("flags/" + projectId, false), blocking);
}

export async function useProjectNotes(projectId: number, blocking = true) {
  return useInitialState("useProjectNotes", () => useInternalApi<Note[]>("projects/notes/" + projectId, false), blocking);
}

export async function useProjectChannels(user: string, project: string, blocking = true) {
  return useInitialState("useProjectChannels", () => useInternalApi<ProjectChannel[]>(`channels/${user}/${project}`, false), blocking);
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
