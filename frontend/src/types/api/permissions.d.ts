declare module "hangar-api" {
  import type { NamedPermission, PermissionType } from "~/types/enums";

  interface IPermission {
    value: NamedPermission;
    frontendName: string;
    permission: bigint;
  }

  interface PermissionCheck {
    type: PermissionType;
    result: boolean;
  }

  interface UserPermissions {
    type: PermissionType;
    permissionBinString: string;
    permissions: IPermission[];
  }
}
