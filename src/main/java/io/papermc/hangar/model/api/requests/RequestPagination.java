package io.papermc.hangar.model.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.papermc.hangar.controller.extras.ApiUtils;
import io.swagger.annotations.ApiModelProperty;

public class RequestPagination {

    @ApiModelProperty(value = "The maximum amount of items to return", example = "1", allowEmptyValue = true, allowableValues = "range[1, 25]")
    private long limit = ApiUtils.limitOrDefault(null);
    @ApiModelProperty(value = "Where to start searching", example = "0", allowEmptyValue = true, allowableValues = "range[0, infinity]")
    private long offset = 0;

    @JsonIgnore
    private Map<String, String> filters = new HashMap<>();
    @JsonIgnore
    private List<String> sorts = new ArrayList<>();

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

    public Map<String, String> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

    public List<String> getSorts() {
        return sorts;
    }

    public void setSorts(List<String> sorts) {
        this.sorts = sorts;
    }

    @Override
    public String toString() {
        return "RequestPagination{" +
                "limit=" + limit +
                ", offset=" + offset +
                '}';
    }
}
