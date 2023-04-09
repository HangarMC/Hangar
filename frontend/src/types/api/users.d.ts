declare module "hangar-api" {
  import type { Model, Named } from "hangar-api";
  import type { NamedPermission, RoleCategory } from "~/types/enums";

  interface Role {
    assignable: boolean;
    rank?: number | null;
    value: string;
    roleId: number;
    roleCategory: RoleCategory;
    permissions: string;
    title: string;
    color: string;
  }

  interface User extends Model, Named {
    tagline: string | null;
    roles: number[];
    projectCount: number;
    isOrganization: boolean;
    locked: boolean;
    nameHistory: { oldName: string; newName: string; date: string }[];
    avatarUrl: string;
    socials?: {
      discord?: string;
      github?: string;
    };
  }

  interface ApiKey extends Model, Named {
    token?: string;
    tokenIdentifier?: string;
    permissions: NamedPermission[];
    lastUsed?: string;
  }
}
