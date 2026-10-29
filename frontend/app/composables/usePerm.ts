import { NamedPermission } from "#shared/types/backend";

/**
 * Checks if the supplier permission has all named permissions.
 * @param perms perms required
 */
export function toNamedPermission(perms: string[]): NamedPermission[] {
  return perms.map((p) => {
    const perm = NamedPermission[p as keyof typeof NamedPermission];
    if (!perm) {
      throw new Error("can't find named permission for " + p);
    }
    return perm;
  });
}

export function hasPerms(...namedPermission: NamedPermission[]): boolean {
  return hasPermsFor(useAuthStore().routePermissions, ...namedPermission);
}

export function hasPermsFor(perms: string | undefined, ...namedPermission: NamedPermission[]): boolean {
  if (!perms) return false;
  const _perms = BigInt("0b" + perms);
  const registeredPerms = useBackendData.permissions;
  if (!registeredPerms || registeredPerms.size === 0) {
    throw new Error("No perms from backend?");
  }
  let result = true;
  for (const np of namedPermission) {
    const perm = registeredPerms.get(np);
    if (!perm) {
      throw new Error(namedPermission + " is not valid");
    }

    const val = BigInt("0b" + perm.permission.toString(2));
    result &&= (_perms & val) === val;
  }
  return result;
}
