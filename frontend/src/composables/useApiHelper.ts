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
  OrganizationRoleTable,
  ProjectChannel,
  ProjectOwner,
  ProjectPage,
  ReviewQueueEntry,
  RoleTable,
} from "hangar-internal";
import { AsyncData } from "nuxt/app";
import { ComputedRef, Ref } from "vue";
import { NuxtApp } from "@nuxt/schema";
import { useApi, useInternalApi } from "~/composables/useApi";
import { ref, useAsyncData, createError } from "#imports";
import { handleRequestError } from "~/composables/useErrorHandling";

export type NonNullAsyncData<T, E = unknown> = { data: Ref<T> } & Pick<AsyncData<T, E>, "pending" | "refresh" | "execute" | "error">;

export async function useProjects(params: Record<string, any> = { limit: 25, offset: 0 }): Promise<Ref<PaginatedResult<Project> | null>> {
  return extract(await useAsyncData("useProjects", () => useApi<PaginatedResult<Project>>("projects", "get", params)));
}

export async function useUser(user: string): Promise<Ref<User | null>> {
  return extract(await useAsyncData("useUser:" + user, () => useApi<User>("users/" + user)));
}

export async function useOrganization(user: string): Promise<Ref<Organization | null>> {
  return extract(await useAsyncData("useOrganization:" + user, () => useInternalApi<Organization>(`organizations/org/${user}`)));
}

export async function useProject(user: string, project: string): Promise<Ref<HangarProject | null>> {
  return extract(await useAsyncData("useProject:" + user + ":" + project, () => useInternalApi<HangarProject>("projects/project/" + user + "/" + project)));
}

export async function useStargazers(user: string, project: string): Promise<Ref<PaginatedResult<User> | null>> {
  return extract(await useAsyncData("useStargazers:" + user + ":" + project, () => useApi<PaginatedResult<User>>(`projects/${user}/${project}/stargazers`)));
}

export async function useWatchers(user: string, project: string): Promise<Ref<PaginatedResult<User> | null>> {
  return extract(await useAsyncData("useWatchers:" + user + ":" + project, () => useApi<PaginatedResult<User>>(`projects/${user}/${project}/watchers`)));
}

export async function useStaff(params?: ComputedRef<{ offset?: number; limit?: number; sort?: string[] }>): Promise<NonNullAsyncData<PaginatedResult<User>>> {
  return await useAsyncDataNonNull("useStaff", () => useApi<PaginatedResult<User>>("staff", "GET", params?.value));
}

export async function useAuthors(params?: { offset?: number; limit?: number; sort?: string[] }): Promise<Ref<PaginatedResult<User> | null>> {
  return extract(await useAsyncData("useAuthors", () => useApi<PaginatedResult<User>>("authors", "GET", params)));
}

export async function useUsers(): Promise<Ref<PaginatedResult<User> | null>> {
  return extract(await useAsyncData("useUsers", () => useApi<PaginatedResult<User>>("users")));
}

export async function useInvites(): Promise<Ref<Invites | null>> {
  return extract(await useAsyncData("useInvites", () => useInternalApi<Invites>("invites")));
}

export async function useNotifications(): Promise<Ref<PaginatedResult<HangarNotification> | null>> {
  return extract(await useAsyncData("useNotifications", () => useInternalApi<PaginatedResult<HangarNotification>>("notifications")));
}

export async function useUnreadNotifications(): Promise<Ref<PaginatedResult<HangarNotification> | null>> {
  return extract(await useAsyncData("useUnreadNotifications", () => useInternalApi<PaginatedResult<HangarNotification>>("unreadnotifications")));
}

export async function useReadNotifications(): Promise<Ref<PaginatedResult<HangarNotification> | null>> {
  return extract(await useAsyncData("useReadNotifications", () => useInternalApi<PaginatedResult<HangarNotification>>("readnotifications")));
}

export async function useRecentNotifications(amount: number): Promise<Ref<HangarNotification[] | null>> {
  return extract(await useAsyncData("useRecentNotifications:" + amount, () => useInternalApi<HangarNotification[]>("recentnotifications?amount=" + amount)));
}

export async function useUnreadNotificationsCount(): Promise<Ref<number | null>> {
  return extract(await useAsyncData("useUnreadNotificationsCount", () => useInternalApi<number>("unreadcount")));
}

export async function useResolvedFlags(): Promise<Ref<PaginatedResult<Flag> | null>> {
  return extract(await useAsyncData("useResolvedFlags", () => useInternalApi<PaginatedResult<Flag>>("flags/resolved")));
}

export async function useUnresolvedFlags(): Promise<Ref<PaginatedResult<Flag> | null>> {
  return extract(await useAsyncData("useUnresolvedFlags", () => useInternalApi<PaginatedResult<Flag>>("flags/unresolved")));
}

export async function useProjectFlags(projectId: number): Promise<Ref<Flag[] | null>> {
  return extract(await useAsyncData("useProjectFlags:" + projectId, () => useInternalApi<Flag[]>("flags/" + projectId)));
}

export async function useProjectNotes(projectId: number): Promise<Ref<Note[] | null>> {
  return extract(await useAsyncData("useProjectNotes:" + projectId, () => useInternalApi<Note[]>("projects/notes/" + projectId)));
}

export async function useProjectChannels(user: string, project: string): Promise<NonNullAsyncData<ProjectChannel[]>> {
  return await useAsyncDataNonNull("useProjectChannels:" + user + ":" + project, () => useInternalApi<ProjectChannel[]>(`channels/${user}/${project}`));
}

export async function useProjectVersions(user: string, project: string): Promise<Ref<PaginatedResult<Version> | null>> {
  return extract(
    await useAsyncData("useProjectVersions:" + user + ":" + project, () => useApi<PaginatedResult<Version>>(`projects/${user}/${project}/versions`))
  );
}

export async function useProjectVersionsInternal(user: string, project: string, version: string): Promise<Ref<HangarVersion | null>> {
  return extract(
    await useAsyncData("useProjectVersionsInternal:" + user + ":" + project + ":" + version, () =>
      useInternalApi<HangarVersion>(`versions/version/${user}/${project}/versions/${version}`)
    )
  );
}

export async function usePage(user: string, project: string, path?: string): Promise<Ref<ProjectPage | null>> {
  path = path?.toString()?.replaceAll(",", "/");
  return extract(
    await useAsyncData("usePage:" + user + ":" + project + ":" + path, () =>
      useInternalApi<ProjectPage>(`pages/page/${user}/${project}` + (path ? "/" + path : ""))
    )
  );
}

export async function useHealthReport(): Promise<Ref<HealthReport | null>> {
  return extract(await useAsyncData("useHealthReport", () => useInternalApi<HealthReport>("admin/health")));
}

export async function useActionLogs(): Promise<Ref<PaginatedResult<LoggedAction> | null>> {
  return extract(await useAsyncData("useActionLogs", () => useInternalApi<PaginatedResult<LoggedAction>>("admin/log")));
}

export async function useVersionApprovals(): Promise<Ref<{ underReview: ReviewQueueEntry[]; notStarted: ReviewQueueEntry[] } | null>> {
  return extract(
    await useAsyncData("useVersionApprovals", () =>
      useInternalApi<{ underReview: ReviewQueueEntry[]; notStarted: ReviewQueueEntry[] }>("admin/approval/versions")
    )
  );
}

export async function usePossibleOwners(): Promise<Ref<ProjectOwner[] | null>> {
  return extract(await useAsyncData("usePossibleOwners", () => useInternalApi<ProjectOwner[]>("projects/possibleOwners")));
}

export async function useOrgVisibility(user: string): Promise<Ref<{ [key: string]: boolean } | null>> {
  return extract(
    await useAsyncData("useOrgVisibility:" + user, () => useInternalApi<{ [key: string]: boolean }>(`organizations/${user}/userOrganizationsVisibility`))
  );
}

export async function useVersionInfo(): Promise<Ref<VersionInfo | null>> {
  return extract(await useAsyncData("useVersionInfo", () => useInternalApi<VersionInfo>(`data/version-info`)));
}

export async function useUserData(
  user: string,
  projectsParams: Record<string, any>
): Promise<
  Ref<{
    starred: PaginatedResult<ProjectCompact>;
    watching: PaginatedResult<ProjectCompact>;
    projects: PaginatedResult<Project>;
    organizations: { [key: string]: OrganizationRoleTable };
    pinned: ProjectCompact[];
  } | null>
> {
  return extract(
    await useAsyncData("useUserData:" + user, async () => {
      // noinspection ES6MissingAwait
      const data = await Promise.all([
        useApi<PaginatedResult<ProjectCompact>>(`users/${user}/starred`),
        useApi<PaginatedResult<ProjectCompact>>(`users/${user}/watching`),
        useApi<PaginatedResult<Project>>(`projects`, "get", {
          owner: user,
          ...projectsParams,
        }),
        useInternalApi<{ [key: string]: RoleTable }>(`organizations/${user}/userOrganizations`),
        useApi<ProjectCompact[]>(`users/${user}/pinned`),
      ]);
      return {
        starred: data[0] as PaginatedResult<ProjectCompact>,
        watching: data[1] as PaginatedResult<ProjectCompact>,
        projects: data[2] as PaginatedResult<Project>,
        organizations: data[3] as { [key: string]: OrganizationRoleTable },
        pinned: data[4] as ProjectCompact[],
      };
    })
  );
}

async function useAsyncDataNonNull<T, E>(key: string, handler: (ctx?: NuxtApp) => Promise<T>): Promise<NonNullAsyncData<T, E>> {
  const asyncData: AsyncData<T, E> = await useAsyncData(key, handler);
  if (asyncData.error.value) {
    handleRequestError(asyncData.error.value, undefined, true);
    throw new Error(`asyncData request had an error for ${key}`);
  }
  if (asyncData.data.value == null) {
    createError({
      statusCode: 400,
      message: `asyncData value was null for ${key}`,
    });
    throw new Error(`asyncData value was null for ${key}`);
  }
  return asyncData as NonNullAsyncData<T, E>;
}

function extract<T, E>(asyncData: AsyncData<T, E>): Ref<T | null> {
  if (asyncData.error?.value) {
    handleRequestError(asyncData.error.value);
    return ref(null);
  } else {
    return asyncData.data;
  }
}
