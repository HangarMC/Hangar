import { Model } from "hangar-api";

declare module "hangar-api" {
  interface Pagination {
    limit: number;
    offset: number;
    count: number;
  }

  interface PaginatedResult<T extends Model> {
    pagination: Pagination;
    result: T[];
  }
}
