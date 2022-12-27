package io.papermc.hangar.model.api;

import io.papermc.hangar.model.api.requests.RequestPagination;

public class Pagination extends RequestPagination {

    private final long count;

    public Pagination(final Long count, final RequestPagination pagination) {
        super(pagination.getLimit(), pagination.getOffset());
        this.count = count != null ? count : 0;
    }

    public long getCount() {
        return this.count;
    }

    @Override
    public String toString() {
        return "Pagination{" +
            "count=" + this.count +
            "} " + super.toString();
    }
}
