import { useApi, useInternalApi } from "~/composables/useApi";
import { PaginatedResult, Project, User } from "hangar-api";
import { useInitialState } from "~/composables/useInitialState";
import { Flag, HangarNotification, HealthReport, Invites, LoggedAction } from "hangar-internal";

export async function useProjects(pagination = { limit: 25, offset: 0 }, blocking = true) {
  return await useInitialState("useProjects", () => useApi<PaginatedResult<Project>>("projects", false, "get", pagination), blocking);
}

export async function useUser(user: string, blocking = true) {
  return await useInitialState("useUser", () => useApi<User>("users/" + user, false), blocking);
}

export async function useProject(user: string, project: string, blocking = true) {
  return await useInitialState("useProject", () => useApi<Project>("projects/" + user + "/" + project, false), blocking);
}

export async function useStargazers(user: string, project: string, blocking = true) {
  return await useInitialState("useStargazers", () => useApi<PaginatedResult<User>>(`projects/${user}/${project}/stargazers`, false), blocking);
}

export async function useWatchers(user: string, project: string, blocking = true) {
  return await useInitialState("useWatchers", () => useApi<PaginatedResult<User>>(`projects/${user}/${project}/watchers`, false), blocking);
}

export async function useStaff(blocking = true) {
  return await useInitialState("useStaff", () => useApi<PaginatedResult<User>>("staff", false), blocking);
}

export async function useAuthors(blocking = true) {
  return await useInitialState("useAuthors", () => useApi<PaginatedResult<User>>("authors", false), blocking);
}

export async function useInvites(blocking = true) {
  return await useInitialState("useInvites", () => useInternalApi<Invites>("invites", false), blocking);
}

export async function useNotifications(blocking = true) {
  return await useInitialState("useNotifications", () => useInternalApi<HangarNotification[]>("notifications", false), blocking);
}

export async function useFlags(blocking = true) {
  return await useInitialState("useFlags", () => useInternalApi<Flag[]>("flags/", false), blocking);
}

export async function useHealthReport(blocking = true) {
  return await useInitialState("useHealthReport", () => useInternalApi<HealthReport>("admin/health", false), blocking);
}

export async function useActionLogs(blocking = true) {
  return await useInitialState("useActionLogs", () => useInternalApi<PaginatedResult<LoggedAction>>("admin/log/", false), blocking);
}
