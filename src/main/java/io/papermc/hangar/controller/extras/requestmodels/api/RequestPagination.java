package io.papermc.hangar.controller.extras.requestmodels.api;

import io.papermc.hangar.controller.extras.ApiUtils;

public class RequestPagination {

    private long limit = ApiUtils.limitOrDefault(null);
    private long offset = 0;

    public RequestPagination() { }

    protected RequestPagination(long limit, long offset) {
        this.limit = limit;
        this.offset = offset;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = ApiUtils.limitOrDefault(limit);
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = ApiUtils.offsetOrZero(offset);
    }
}
