package io.papermc.hangar.model.api;

import java.util.List;

public record PaginatedResult<T>(Pagination pagination, List<T> result) {
}
