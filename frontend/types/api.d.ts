/* eslint-disable camelcase */
declare module 'hangar-api' {
    import { ApiSessionType, ProjectCategory, RoleCategory, Visibility } from '~/types/enums';

    interface ApiError {
        message: string;
        error: {
            message: string;
            code: number;
        };
        pathParams: Record<string, string>;
    }

    interface Model {
        id: number;
        createdAt: string;
    }

    interface Named {
        name: string;
    }

    interface Color {
        value: number;
        hex: string;
    }

    interface TagColor {
        background: string;
        foreground: string;
    }

    interface Role {
        value: string;
        roleId: number;
        category: RoleCategory;
        permission: bigint; // TODO maybe?
        title: string;
        color: Color;
    }

    interface HeaderData {
        globalPermission: string;
        hasNotice: boolean;
        hasProjectApprovals: boolean;
        hasReviewQueue: boolean;
        hasUnreadNotifications: boolean;
        unresolvedFlags: boolean;
    }

    interface User extends Model, Named {
        tagline: string | null;
        joinDate: string;
        roles: Role[];
        headerData: HeaderData;
    }

    interface ApiSession {
        session: string;
        expires: string;
        type: ApiSessionType;
    }

    interface Pagination {
        limit: number;
        offset: number;
        count: number;
    }

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
        waters: number;
    }

    interface UserActions {
        starred: boolean;
        watching: boolean;
    }

    interface ProjectSettings {
        homepage: string | null;
        issues: string | null;
        sources: string | null;
        support: string | null;
        license: string | null;
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

    interface PaginatedResult<T extends Model> {
        pagination: Pagination;
        result: T[];
    }

    interface Announcement {
        text: String;
        color: String;
    }

    interface Sponsor {
        img: String;
        title: String;
        link: String;
    }

    // Data interfaces
    interface IProjectCategory {
        title: string;
        icon: string;
        apiName: string;
        visible: boolean;
    }

    interface IPermission {
        value: string;
        frontendName: string;
        permission: bigint;
    }
}
