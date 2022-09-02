package io.papermc.hangar.model.api;

import java.util.List;

public class PaginatedResult<T> {

    private final Pagination pagination;
    private final List<T> result;

    public PaginatedResult(Pagination pagination, List<T> result) {
        this.pagination = pagination;
        this.result = result;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public List<T> getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "PaginatedResult{" +
                "pagination=" + pagination +
                ", result=" + result +
                '}';
    }
}
