declare module 'hangar-internal' {
    import { Named, Role, User } from 'hangar-api';
    import { ProjectOwner, Table } from 'hangar-internal';
    import { RoleCategory } from '~/types/enums';

    interface HangarNotification {
        id: number;
        action: string;
        message: string[];
        read: boolean;
        originUserName: string | null;
        type: string; // TODO enum really needed? its just for css styling (at least right now)
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

    interface JoinableMember {
        user: UserTable;
        role: RoleTable;
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
