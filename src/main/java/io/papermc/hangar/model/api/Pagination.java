package io.papermc.hangar.model.api;

import io.papermc.hangar.model.api.requests.RequestPagination;

public class Pagination extends RequestPagination {

    private final long count;

    public Pagination(long limit, long offset, Long count) {
        super(limit, offset);
        this.count = count != null ? count : 0;
    }

    public Pagination(Long count, RequestPagination pagination) {
        this(pagination.getLimit(), pagination.getOffset(), count);
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
