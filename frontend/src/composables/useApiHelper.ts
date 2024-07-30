import type { AsyncData, NuxtApp } from "nuxt/app";
import type { ComputedRef, Ref } from "vue";
import type { _AsyncData } from "#app/composables/asyncData";
import {
  type HangarChannel,
  type HangarNotification,
  type HangarOrganization,
  type HangarProject,
  type HangarProjectFlag,
  type HangarProjectNote,
  type HangarProjectPage,
  type HangarVersion,
  type HealthReport,
  type JarScanResult,
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
  type SettingsResponse,
  type User,
  type VersionInfo,
  type Invites,
  type ReviewQueue,
} from "~/types/backend";

export type NonNullAsyncData<T, E = unknown> = { data: Ref<T> } & Pick<AsyncData<T, E>, "pending" | "refresh" | "execute" | "error">;

export async function useProjects(params: Record<string, any> = { limit: 25, offset: 0 }): Promise<Ref<PaginatedResultProject | null>> {
  return extract(await useAsyncData("useProjects", () => useApi<PaginatedResultProject>("projects", "get", params)));
}

export async function useUser(user: string): Promise<Ref<User | null>> {
  return extract(await useAsyncData("useUser:" + user, () => useApi<User>("users/" + user)));
}

export async function useOrganization(user: string): Promise<Ref<HangarOrganization | null>> {
  return extract(await useAsyncData("useOrganization:" + user, () => useInternalApi<HangarOrganization>(`organizations/org/${user}`)));
}

export async function useProject(project: string): Promise<Ref<HangarProject | null>> {
  return extract(await useAsyncData("useProject:" + project, () => useInternalApi<HangarProject>("projects/project/" + project)));
}

export async function useStargazers(project: string): Promise<Ref<PaginatedResultUser | null>> {
  return extract(await useAsyncData("useStargazers:" + project, () => useApi<PaginatedResultUser>(`projects/${project}/stargazers`)));
}

export async function useWatchers(project: string): Promise<Ref<PaginatedResultUser | null>> {
  return extract(await useAsyncData("useWatchers:" + project, () => useApi<PaginatedResultUser>(`projects/${project}/watchers`)));
}

export async function useStaff(params?: ComputedRef<{ offset?: number; limit?: number; sort?: string[] }>): Promise<NonNullAsyncData<PaginatedResultUser>> {
  return await useAsyncDataNonNull("useStaff", () => useApi<PaginatedResultUser>("staff", "GET", params?.value));
}

export async function useAuthors(params?: { offset?: number; limit?: number; sort?: string[] }): Promise<Ref<PaginatedResultUser | null>> {
  return extract(await useAsyncData("useAuthors", () => useApi<PaginatedResultUser>("authors", "GET", params)));
}

export async function useUsers(): Promise<Ref<PaginatedResultUser | null>> {
  return extract(await useAsyncData("useUsers", () => useApi<PaginatedResultUser>("users")));
}

export async function useInvites(): Promise<Ref<Invites | null>> {
  return extract(await useAsyncData("useInvites", () => useInternalApi<Invites>("invites")));
}

export async function useNotifications(): Promise<Ref<PaginatedResultHangarNotification | null>> {
  return extract(await useAsyncData("useNotifications", () => useInternalApi<PaginatedResultHangarNotification>("notifications")));
}

export async function useUnreadNotifications(): Promise<Ref<PaginatedResultHangarNotification | null>> {
  return extract(await useAsyncData("useUnreadNotifications", () => useInternalApi<PaginatedResultHangarNotification>("unreadnotifications")));
}

export async function useReadNotifications(): Promise<Ref<PaginatedResultHangarNotification | null>> {
  return extract(await useAsyncData("useReadNotifications", () => useInternalApi<PaginatedResultHangarNotification>("readnotifications")));
}

export async function useRecentNotifications(amount: number): Promise<Ref<HangarNotification[] | null>> {
  return extract(await useAsyncData("useRecentNotifications:" + amount, () => useInternalApi<HangarNotification[]>("recentnotifications?amount=" + amount)));
}

export async function useUnreadNotificationsCount(): Promise<Ref<number | null>> {
  return extract(await useAsyncData("useUnreadNotificationsCount", () => useInternalApi<number>("unreadcount")));
}

export async function useResolvedFlags(): Promise<Ref<PaginatedResultHangarProjectFlag | null>> {
  return extract(await useAsyncData("useResolvedFlags", () => useInternalApi<PaginatedResultHangarProjectFlag>("flags/resolved")));
}

export async function useUnresolvedFlags(): Promise<Ref<PaginatedResultHangarProjectFlag | null>> {
  return extract(await useAsyncData("useUnresolvedFlags", () => useInternalApi<PaginatedResultHangarProjectFlag>("flags/unresolved")));
}

export async function useProjectFlags(projectId: number): Promise<Ref<HangarProjectFlag[] | null>> {
  return extract(await useAsyncData("useProjectFlags:" + projectId, () => useInternalApi<HangarProjectFlag[]>("flags/" + projectId)));
}

export async function useProjectNotes(projectId: number): Promise<Ref<HangarProjectNote[] | null>> {
  return extract(await useAsyncData("useProjectNotes:" + projectId, () => useInternalApi<HangarProjectNote[]>("projects/notes/" + projectId)));
}

export async function useProjectChannels(project: string): Promise<NonNullAsyncData<HangarChannel[]>> {
  return await useAsyncDataNonNull("useProjectChannels:" + project, () => useInternalApi<HangarChannel[]>(`channels/${project}`));
}

export async function useProjectVersions(project: string, data?: object): Promise<Ref<PaginatedResultVersion | null>> {
  return extract(await useAsyncData("useProjectVersions:" + project, () => useApi<PaginatedResultVersion>(`projects/${project}/versions`, "GET", data)));
}

export async function useProjectVersionsInternal(project: string, version: string): Promise<Ref<HangarVersion | null>> {
  return extract(
    await useAsyncData("useProjectVersionsInternal:" + project + ":" + version, () =>
      useInternalApi<HangarVersion>(`versions/version/${project}/versions/${version}`)
    )
  );
}

export async function usePage(project: string, path?: string | null): Promise<Ref<HangarProjectPage | null>> {
  path = path?.toString()?.replaceAll(",", "/");
  return extract(
    await useAsyncData("usePage:" + project + ":" + path, () => useInternalApi<HangarProjectPage>(`pages/page/${project}` + (path ? "/" + path : "")))
  );
}

export async function useHealthReport(): Promise<Ref<HealthReport | null>> {
  return extract(await useAsyncData("useHealthReport", () => useInternalApi<HealthReport>("admin/health", "GET", undefined, { timeout: 60_000 })));
}

export async function useActionLogs(): Promise<Ref<PaginatedResultHangarLoggedAction | null>> {
  return extract(await useAsyncData("useActionLogs", () => useInternalApi<PaginatedResultHangarLoggedAction>("admin/log")));
}

export async function useVersionApprovals(): Promise<Ref<ReviewQueue | null>> {
  return extract(await useAsyncData("useVersionApprovals", () => useInternalApi<ReviewQueue>("admin/approval/versions")));
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

export async function useJarScan(versionId: number, platform: string): Promise<Ref<JarScanResult | null>> {
  return extract(
    await useAsyncData("useJarScan:" + versionId + ":" + platform, () => useInternalApi<JarScanResult>(`jarscanning/result/${platform}/${versionId}`))
  );
}

export async function useAuthSettings(): Promise<Ref<SettingsResponse | null>> {
  return extract(await useAsyncData("useAuthSettings", () => useInternalApi<SettingsResponse>(`auth/settings`, "POST")));
}

export async function useUserData(
  user: string,
  projectsParams: Record<string, any>
): Promise<
  Ref<{
    starred: PaginatedResultProjectCompact;
    watching: PaginatedResultProjectCompact;
    projects: PaginatedResultProject;
    organizations: { [key: string]: OrganizationRoleTable };
    pinned: ProjectCompact[];
    organizationVisibility: { [key: string]: boolean } | null;
    possibleAlts: string[] | null;
  } | null>
> {
  const self = user === useAuthStore().user?.name;
  return extract(
    await useAsyncData("useUserData:" + user, async () => {
      // noinspection ES6MissingAwait
      const data = await Promise.all([
        useApi<PaginatedResultProjectCompact>(`users/${user}/starred`),
        useApi<PaginatedResultProjectCompact>(`users/${user}/watching`),
        useApi<PaginatedResultProject>(`projects`, "get", {
          member: user,
          ...projectsParams,
        }),
        useInternalApi<{ [key: string]: OrganizationRoleTable }>(`organizations/${user}/userOrganizations`),
        useApi<ProjectCompact[]>(`users/${user}/pinned`),
        self ? useOrgVisibility(user) : null,
        hasPerms(NamedPermission.IsStaff) ? useInternalApi<string[]>(`users/${user}/alts`) : null,
      ]);
      return {
        starred: data[0] as PaginatedResultProjectCompact,
        watching: data[1] as PaginatedResultProjectCompact,
        projects: data[2] as PaginatedResultProject,
        organizations: data[3] as { [key: string]: OrganizationRoleTable },
        pinned: data[4] as ProjectCompact[],
        organizationVisibility: data[5],
        possibleAlts: data[6],
      };
    })
  );
}

async function useAsyncDataNonNull<T, E>(key: string, handler: (ctx?: NuxtApp) => Promise<T>): Promise<NonNullAsyncData<T, E>> {
  const asyncData = await useAsyncData<T, E>(key, handler);
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

function extract<T, E>(asyncData: _AsyncData<T, E>): Ref<T | null> {
  if (asyncData.error?.value) {
    handleRequestError(asyncData.error.value);
    return ref(null);
  } else {
    return asyncData.data;
  }
}
