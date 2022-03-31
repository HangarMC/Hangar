declare module "hangar-internal" {
  import type { Named, Role, User } from "hangar-api";
  import type { ProjectOwner, Table } from "hangar-internal";
  import type { RoleCategory } from "~/types/enums";

  interface HangarNotification {
    id: number;
    action: string;
    message: string[];
    read: boolean;
    originUserName: string | null;
    type: string;
  }

  interface Invite {
    roleTableId: number;
    role: Role;
    type: "project" | "organization";
    name: string;
    url: string;
    accepted?: boolean;
  }

  interface Invites {
    project: Invite[];
    organization: Invite[];
  }

  interface HeaderData {
    globalPermission: string;
    unreadNotifications: number;
    unansweredInvites: number;
    unresolvedFlags: number;
    projectApprovals: number;
    reviewQueueCount: number;
    organizationCount: number;
  }

  interface HangarUser extends User, Table {
    headerData: HeaderData;
    readPrompts: number[];
    language: string;
  }

  interface UserTable extends Table, Named {
    tagline: string;
    joinDate: string;
    readPrompts: number[];
    locked: boolean;
    language: string;
  }

  interface RoleTable extends Table {
    accepted: boolean;
    principalId: number;
    userId: number;
    role: Role;
  }

  interface OrganizationRoleTable extends RoleTable {
    accepted: boolean;
    principalId: number;
    userId: number;
    role: Role;
    ownerName: string;
    ownerId: number;
  }

  interface JoinableMember {
    user: UserTable;
    role: RoleTable;
    hidden: boolean;
  }

  interface Joinable {
    owner: ProjectOwner;
    roleCategory: RoleCategory;
    members: JoinableMember[];
  }

  interface Organization extends Joinable {
    id: number;
  }
}
