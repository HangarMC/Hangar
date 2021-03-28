declare module 'hangar-internal' {
    import { FlagReason, Joinable, Table } from 'hangar-internal';
    import { Project } from 'hangar-api';
    import { ProjectCategory, Visibility } from '~/types/enums';

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

    interface HangarProject extends Joinable, Project, Table {
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
        isHome: boolean;
    }

    interface Flag extends Table {
        userId: number;
        reportedByName: string;
        reason: FlagReason;
        isResolved: boolean;
        comment: string;
        resolvedAt: string;
        resolvedBy: number;
        resolvedByName: string;
        projectId: number;
        projectOwnerName: string;
        projectSlug: string;
        projectNamespace: string;
        projectVisibility: Visibility;
    }

    interface Note extends Table {
        projectId: number;
        message: string;
        userId: number;
        userName: string | null;
    }

    interface ProjectSettingsForm {
        settings: {
            homepage: string | null;
            issues: string | null;
            source: string | null;
            support: string | null;
            keywords: string[];
            license: {
                type?: string | null;
                url: string | null;
                name: string | null;
            };
            forumSync: false;
        };
        category: ProjectCategory;
        description: string;
    }
}
