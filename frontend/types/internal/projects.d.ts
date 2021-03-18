declare module 'hangar-internal' {
    import { FlagReason, Table } from 'hangar-internal';
    import { Project, Role, User } from 'hangar-api';
    import { ProjectCategory } from '~/types/enums';

    interface ProjectOwner {
        id: number;
        name: string;
        userId: number;
        isOrganization: boolean;
    }

    interface HangarProjectInfo {
        publicVersions: number;
        flagCount: number;
        noteCount: number;
        starCount: number;
        watcherCount: number;
    }

    interface HangarProjectPage {
        id: number;
        name: string;
        slug: string;
        home: boolean;
        children: HangarProjectPage[];
    }

    interface ProjectRole extends Table {
        accepted: true;
        role: Role;
    }

    interface ProjectMember {
        user: User;
        role: ProjectRole;
    }

    interface HangarProject extends Project, Table {
        owner: ProjectOwner;
        members: ProjectMember[];
        lastVisibilityChangeComment: string;
        lastVisibilityChangeUserName: string;
        info: HangarProjectInfo;
        pages: HangarProjectPage[];
    }

    interface ProjectPage extends Table {
        name: string;
        slug: string;
        contents: string;
        deletable: boolean;
    }

    interface Flag extends Table {
        user: User;
        reason: FlagReason;
        resolved: boolean;
        comment: string;
        resolvedAt: string;
        resolvedBy: User;
    }

    interface Note extends Table {
        message: string;
        user: User;
    }

    interface ProjectSettingsForm {
        settings: {
            homepage: string | null;
            issues: string | null;
            source: string | null;
            support: string | null;
            keywords: string[];
            license: {
                type: string;
                url: string;
                customName: string;
            };
            forumSync: false;
        };
        category: ProjectCategory;
        description: string;
    }
}
