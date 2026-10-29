import backendData from "#shared/generated/backendData.json";
import type { Option } from "#shared/types/components/ui/InputAutocomplete";
import { NamedPermission } from "#shared/types/backend";
import type { CategoryData, PermissionData, PermissionGroup, RoleData } from "#shared/types/backend";
import type { BackendData, ServerBackendData } from "#shared/types/backendData";

const serverBackendData = { ...backendData } as unknown as ServerBackendData;
const typedBackendData = { ...serverBackendData } as unknown as BackendData;

// backendData.json is a build-time snapshot and may predate these endpoints; mirrors the backend MemberPermissions
const PROJECT_PERMISSIONS = [NamedPermission.EditPage, NamedPermission.EditChannels, NamedPermission.DeleteProject];
const VERSION_PERMISSIONS = [NamedPermission.CreateVersion, NamedPermission.EditVersion, NamedPermission.DeleteVersion];
typedBackendData.projectPermissions ??= [
  { name: "project", permissions: [NamedPermission.EditSubjectSettings, ...PROJECT_PERMISSIONS] },
  { name: "versions", permissions: VERSION_PERMISSIONS },
  { name: "people", permissions: [NamedPermission.ManageSubjectMembers] },
] satisfies PermissionGroup[];
typedBackendData.organizationPermissions ??= [
  { name: "organization", permissions: [NamedPermission.EditSubjectSettings] },
  { name: "projects", permissions: [NamedPermission.CreateProject, ...PROJECT_PERMISSIONS] },
  { name: "versions", permissions: VERSION_PERMISSIONS },
  { name: "people", permissions: [NamedPermission.ManageSubjectMembers] },
] satisfies PermissionGroup[];

// convert to bigint
const permissionResult = serverBackendData.permissions?.map(
  ({ value, frontendName, permission }) =>
    ({
      value,
      frontendName,
      permission: BigInt("0b" + permission),
    }) as PermissionData
);

// convert to maps
typedBackendData.projectCategories = convertToMap(serverBackendData.projectCategories, (value) => value.apiName);
typedBackendData.permissions = convertToMap(permissionResult, (value) => value.value);
typedBackendData.prompts = convertToMap(serverBackendData.prompts, (value) => value.name);

// main export
export const useBackendData = typedBackendData;

export const ORGANIZATION_ROLE = "Organization";

export function getRole(id?: number): RoleData | undefined {
  if (!id) return undefined;
  return getRoleFromRoles(id, typedBackendData.globalRoles);
}

// the organization role marks the account type rather than a granted role, so it never belongs in a role tag list
export function displayRoles(roleIds?: number[]): RoleData[] {
  return (roleIds ?? []).map((roleId) => getRole(roleId)).filter((role): role is RoleData => role !== undefined && role.value !== ORGANIZATION_ROLE);
}

export function getRoleByValue(id: string): RoleData | undefined {
  return getRoleFromRolesValue(id, typedBackendData.globalRoles);
}

function getRoleFromRolesValue(id: string, roles: RoleData[]): RoleData | undefined {
  return roles?.find((r) => r.value === id);
}

function getRoleFromRoles(id: number, roles: RoleData[]): RoleData | undefined {
  return roles?.find((r) => r.roleId === id);
}

// helpers
export const useVisibleCategories = computed<CategoryData[]>(() => [...(useBackendData.projectCategories?.values() || [])].filter((value) => value.visible));

export const useLicenseOptions = computed<Option<string>[]>(() => useBackendData.licenses.map<Option<string>>((l) => ({ value: l, text: l })));
export const useCategoryOptions = computed<Option<string>[]>(() =>
  useVisibleCategories.value.map<Option<string>>((c) => ({ value: c.apiName, text: c.title }))
);

function convertToMap<E, T>(values: T[] = [], toStringFunc: (value: T) => string): Map<E, T> {
  const map = new Map<E, T>();
  for (const value of values) {
    const key: E = toStringFunc(value) as unknown as E;
    if (key == undefined) {
      throw new Error("Could not find an enum for " + value);
    }
    map.set(key, value);
  }
  return map;
}
