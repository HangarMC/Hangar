declare module "hangar-api" {
  import type { Visibility } from "~/types/enums";

  interface Model {
    createdAt: string;
  }

  interface Named {
    name: string;
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
    text: string;
    color: string;
  }

  interface Sponsor {
    image: string;
    name: string;
    link: string;
  }

  interface Visible {
    visibility: Visibility;
  }
}
