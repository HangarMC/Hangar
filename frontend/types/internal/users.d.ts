declare module 'hangar-internal' {
    import { Named, Role, User } from 'hangar-api';
    import { Table } from 'hangar-internal';

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
    }

    interface Invites {
        projects: Invite[];
        organizations: Invite[];
    }

    interface HeaderData {
        globalPermission: string;
        unreadNotifications: number;
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
        projectId: number;
        userId: number;
        role: Role;
    }
}
