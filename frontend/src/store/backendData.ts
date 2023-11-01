import type { BackendData, IPermission, Role } from "hangar-api";
import { computed } from "vue";
import type { IPlatform, IProjectCategory, IPrompt } from "hangar-internal";
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import backendData from "~/generated/backendData.json";
import type { Option } from "~/types/components/ui/InputAutocomplete";

const typedBackendData = { ...backendData } as unknown as BackendData;

// convert to bigint
const permissionResult = (typedBackendData.permissions as unknown as IPermission[])?.map(({ value, frontendName, permission }) => ({
  value,
  frontendName,
  permission: BigInt("0b" + permission),
}));

// convert to maps
typedBackendData.projectCategories = convertToMap(typedBackendData.projectCategories as unknown as IProjectCategory[], (value) => value.apiName);
typedBackendData.permissions = convertToMap(permissionResult, (value) => value.value);
typedBackendData.platforms = convertToMap(typedBackendData.platforms as unknown as IPlatform[], (value) => value.name.toUpperCase());
typedBackendData.prompts = convertToMap(typedBackendData.prompts as unknown as IPrompt[], (value) => value.name);

// main export
export const useBackendData = typedBackendData;

export function getRole(id: number): Role | undefined {
  return (
    getRoleFromRoles(id, typedBackendData.globalRoles) || getRoleFromRoles(id, typedBackendData.projectRoles) || getRoleFromRoles(id, typedBackendData.orgRoles)
  );
}

export function getRoleByValue(id: string): Role | undefined {
  return (
    getRoleFromRolesValue(id, typedBackendData.globalRoles) ||
    getRoleFromRolesValue(id, typedBackendData.projectRoles) ||
    getRoleFromRolesValue(id, typedBackendData.orgRoles)
  );
}

function getRoleFromRolesValue(id: string, roles: Role[]): Role | undefined {
  return roles.find((r) => r.value === id);
}

function getRoleFromRoles(id: number, roles: Role[]): Role | undefined {
  return roles.find((r) => r.roleId === id);
}

// helpers
export const useVisibleCategories = computed<IProjectCategory[]>(() =>
  [...(useBackendData.projectCategories?.values() || [])].filter((value) => value.visible)
);
export const useVisiblePlatforms = computed(() => (useBackendData.platforms ? [...useBackendData.platforms.values()].filter((value) => value.visible) : []));

export const useLicenseOptions = computed<Option[]>(() => useBackendData.licenses.map<Option>((l) => ({ value: l, text: l })));
export const useCategoryOptions = computed<Option[]>(() => useVisibleCategories.value.map<Option>((c) => ({ value: c.apiName, text: c.title })));

function convertToMap<E, T>(values: T[] = [], toStringFunc: (value: T) => string): Map<E, T> {
  const map = new Map<E, T>();
  for (const value of values) {
    const key: E = toStringFunc(value) as unknown as E;
    if (key == null) {
      throw new Error("Could not find an enum for " + value);
    }
    map.set(key, value);
  }
  return map;
}
