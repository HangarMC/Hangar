package io.papermc.hangar.model.api;

import io.papermc.hangar.model.api.requests.RequestPagination;

public class Pagination extends RequestPagination {

    private final long count;

    public Pagination(Long count, RequestPagination pagination) {
        super(pagination.getLimit(), pagination.getOffset());
        this.count = count != null ? count : 0;
    }

    public long getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "Pagination{" +
                "count=" + count +
                "} " + super.toString();
    }
}
