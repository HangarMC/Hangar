declare module 'hangar-api' {
    import { Model, Named, TagColor, Visible } from 'hangar-api';
    import { ProjectCategory } from '~/types/enums';

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
        flagged: boolean;
    }

    interface License {
        name: string | null;
        url: string | null;
        type: string | null;
    }

    interface ProjectSettings {
        homepage: string | null;
        issues: string | null;
        source: string | null;
        support: string | null;
        license: License;
        keywords: string[];
        forumSync: boolean;
        donation: {
            enable: false;
            email: string | null;
            defaultAmount: number;
            oneTimeAmounts: Array<number>;
            monthlyAmounts: Array<number>;
        };
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

    interface ProjectCompact extends Model, Named, Visible {
        namespace: ProjectNamespace;
        stats: ProjectStats;
        category: ProjectCategory;
    }

    interface Project extends ProjectCompact {
        description: string;
        lastUpdated: Date;
        userActions: UserActions;
        settings: ProjectSettings;
        postId: number;
        topicId: number;
        promotedVersions: PromotedVersion[];
    }
}
