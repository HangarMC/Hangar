declare module 'hangar-api' {
    import { Model, Named, TagColor } from 'hangar-api';
    import { ProjectCategory, Visibility } from '~/types/enums';

    interface ProjectNamespace {
        owner: string;
        slug: string;
    }

    interface ProjectStats {
        views: number;
        downloads: number;
        recentViews: number;
        recentDownloads: number;
        stars: number;
        watchers: number;
    }

    interface UserActions {
        starred: boolean;
        watching: boolean;
    }

    interface Licence {
        name: string | null;
        url: string | null;
    }

    interface ProjectSettings {
        homepage: string | null;
        issues: string | null;
        sources: string | null;
        support: string | null;
        license: Licence | null;
        forumSync: boolean;
    }

    interface PromotedVersionTag extends Named {
        data: string;
        displayData: string;
        minecraftVersion: string;
        color: TagColor;
    }

    interface PromotedVersion {
        version: string;
        tags: PromotedVersionTag[];
    }

    interface Project extends Model, Named {
        namespace: ProjectNamespace;
        promotedVersions: PromotedVersion[];
        stats: ProjectStats;
        category: ProjectCategory;
        description: string;
        lastUpdated: Date;
        visibility: Visibility;
        userActions: UserActions;
        settings: ProjectSettings;
        iconUrl: string;
    }
}
