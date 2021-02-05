declare module 'hangar-internal' {
    import { Role } from 'hangar-api';

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
}
