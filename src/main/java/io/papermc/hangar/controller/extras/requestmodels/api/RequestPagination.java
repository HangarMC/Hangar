package io.papermc.hangar.controller.extras.requestmodels.api;

import io.papermc.hangar.controller.extras.ApiUtils;
import io.swagger.annotations.ApiModelProperty;

public class RequestPagination {

    @ApiModelProperty(value = "The maximum amount of items to return", example = "1", allowEmptyValue = true, allowableValues = "range[1, 25]")
    private long limit = ApiUtils.limitOrDefault(null);
    @ApiModelProperty(value = "Where to start searching", example = "0", allowEmptyValue = true, allowableValues = "range[0, infinity]")
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

    @Override
    public String toString() {
        return "RequestPagination{" +
                "limit=" + limit +
                ", offset=" + offset +
                '}';
    }
}
