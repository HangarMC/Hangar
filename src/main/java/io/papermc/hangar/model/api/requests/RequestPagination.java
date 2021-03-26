package io.papermc.hangar.model.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.controller.extras.ApiUtils;
import io.papermc.hangar.controller.extras.pagination.filters.Filter.FilterInstance;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RequestPagination {

    @ApiModelProperty(value = "The maximum amount of items to return", example = "1", allowEmptyValue = true, allowableValues = "range[1, 25]")
    private long limit = ApiUtils.limitOrDefault(null);

    @ApiModelProperty(value = "Where to start searching", example = "0", allowEmptyValue = true, allowableValues = "range[0, infinity]")
    private long offset = 0;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private final List<FilterInstance> filters;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private final List<Consumer<StringBuilder>> sorters;

    public RequestPagination() {
        this.filters = new ArrayList<>();
        this.sorters = new ArrayList<>();
    }

    protected RequestPagination(long limit, long offset) {
        this.limit = limit;
        this.offset = offset;
        this.filters = new ArrayList<>();
        this.sorters = new ArrayList<>();
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

    public List<FilterInstance> getFilters() {
        return filters;
    }

    public List<Consumer<StringBuilder>> getSorters() {
        return sorters;
    }

    @Override
    public String toString() {
        return "RequestPagination{" +
                "limit=" + limit +
                ", offset=" + offset +
                '}';
    }
}
