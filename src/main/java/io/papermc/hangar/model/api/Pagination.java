package io.papermc.hangar.model.api;

import io.papermc.hangar.controller.requestmodels.api.RequestPagination;

public class Pagination extends RequestPagination {

    private final long count;

    public Pagination(long limit, long offset, long count) {
        super(limit, offset);
        this.count = count;
    }

    public Pagination(long count, RequestPagination pagination) {
        this(pagination.getLimit(), pagination.getOffset(), count);
    }

    public long getCount() {
        return count;
    }
}
