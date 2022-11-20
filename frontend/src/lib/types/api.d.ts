declare module "hangar-api" {
  interface Model {
    createdAt: string;
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
}
