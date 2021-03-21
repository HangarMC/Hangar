declare module 'hangar-internal' {
    import { Named, Role, User } from 'hangar-api';
    import { ProjectOwner, Table } from 'hangar-internal';
    import { RoleCategory } from '~/types/enums';

    interface HangarNotification {
        id: number;
        type: 'PROJECT_INVITE' | 'ORGANIZATION_INVITE' | 'NEW_PROJECT_VERSION' | 'VERSION_REVIEWED';
        action: string;
        message: string[];
        read: boolean;
        originUserName: string | null;
    }

    interface Invite {
        roleTableId: number;
        role: Role;
        type: 'project' | 'organization';
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
    }

    interface HangarUser extends User, Table {
        headerData: HeaderData;
        readPrompts: number[];
        locked: boolean;
        language: string;
        isOrganization: boolean;
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

    interface JoinableMember {
        user: UserTable;
        role: RoleTable;
    }

    interface Joinable {
        owner: ProjectOwner;
        roleCategory: RoleCategory;
        members: JoinableMember[];
    }
}
