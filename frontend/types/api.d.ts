/* eslint-disable camelcase */
declare module 'hangar-api' {
    import { ApiSessionType, ProjectCategory, RoleCategory, Visibility } from '~/types/enums';

    interface Model {
        id: number;
        created_at: string;
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
        role_id: number;
        category: RoleCategory;
        permission: bigint; // TODO maybe?
        title: string;
        color: Color;
    }

    interface User extends Model, Named {
        tagline: string | null;
        joinDate: string;
        roles: Role[];
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
        recent_views: number;
        recent_downloads: number;
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
        minecraft_version: string;
        color: TagColor;
    }

    interface PromotedVersion {
        version: string;
        tags: PromotedVersionTag[];
    }

    interface Project extends Model, Named {
        namespace: ProjectNamespace;
        promoted_versions: PromotedVersion[];
        stats: ProjectStats;
        category: ProjectCategory;
        description: string;
        last_updated: Date;
        visibility: Visibility;
        user_actions: UserActions;
        settings: ProjectSettings;
        icon_url: string;
    }

    interface PaginatedProjectList {
        pagination: Pagination;
        result: Project[];
    }

    interface Announcement {
        text: String;
        color: String;
    }
}
