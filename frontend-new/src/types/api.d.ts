declare module 'hangar-api' {
    import type { Visibility } from '~/types/enums';

    interface Model {
        createdAt: string;
    }

    interface Named {
        name: string;
    }

    interface TagColor {
        background: string;
        foreground: string;
    }

    interface Pagination {
        limit: number;
        offset: number;
        count: number;
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
        image: String;
        name: String;
        link: String;
    }

    interface Visible {
        visibility: Visibility;
    }
}
