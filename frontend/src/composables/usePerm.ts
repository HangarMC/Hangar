/**
 * Checks if the supplier permission has all named permissions.
 * @param namedPermission perms required
 */
import { useAuthStore } from "~/store/auth";
import { useBackendData } from "~/store/backendData";
import { NamedPermission } from "~/types/enums";

export function toNamedPermission(perms: string[]): NamedPermission[] {
  return perms.map((p) => {
    const perm = NamedPermission[p as keyof typeof NamedPermission];
    if (!perm) {
      console.log("can't find named permission for", p);
    }
    return perm;
  });
}

export function hasPerms(...namedPermission: NamedPermission[]): boolean {
  const perms = useAuthStore().routePermissions;
  if (!perms) return false;
  const _perms = BigInt("0b" + perms);
  let result = true;
  const registeredPerms = useBackendData.permissions;
  if (!registeredPerms || registeredPerms.size === 0) {
    throw new Error("No perms from backend?");
  }
  for (const np of namedPermission) {
    const perm = registeredPerms.get(np);
    if (!perm) {
      throw new Error(namedPermission + " is not valid");
    }
    const val = BigInt("0b" + perm.permission.toString(2));
    result = result && (_perms & val) === val;
  }
  return result;
}
