import { useApi, useInternalApi } from "~/composables/useApi";
import { PaginatedResult, Project, User } from "hangar-api";
import { useInitialState } from "~/composables/useInitialState";
import { HangarNotification, Invites } from "hangar-internal";

export async function useProjects(pagination = { limit: 25, offset: 0 }, blocking = true) {
  return await useInitialState("useProjects", () => useApi<PaginatedResult<Project>>("projects", false, "get", pagination), blocking);
}

export async function useUser(user: string, blocking = true) {
  return await useInitialState("useUser", () => useApi<User>("users/" + user, false), blocking);
}

export async function useProject(user: string, project: string, blocking = true) {
  return await useInitialState("useProject", () => useApi<Project>("projects/" + user + "/" + project, false), blocking);
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
