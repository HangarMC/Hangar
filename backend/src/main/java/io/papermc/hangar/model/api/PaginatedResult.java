package io.papermc.hangar.model.api;

import java.util.List;

public class PaginatedResult<T> {

    private final Pagination pagination;
    private final List<T> result;

    public PaginatedResult(final Pagination pagination, final List<T> result) {
        this.pagination = pagination;
        this.result = result;
    }

    public Pagination getPagination() {
        return this.pagination;
    }

    public List<T> getResult() {
        return this.result;
    }

    @Override
    public String toString() {
        return "PaginatedResult{" +
                "pagination=" + this.pagination +
                ", result=" + this.result +
                '}';
    }
}
