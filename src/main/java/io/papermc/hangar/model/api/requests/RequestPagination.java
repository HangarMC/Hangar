package io.papermc.hangar.model.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.controller.extras.ApiUtils;
import io.papermc.hangar.controller.extras.pagination.Filter.FilterInstance;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RequestPagination {

    @ApiModelProperty(value = "The maximum amount of items to return", example = "1", allowEmptyValue = true, allowableValues = "range[1, 25]")
    private final long limit;

    @ApiModelProperty(value = "Where to start searching", example = "0", allowEmptyValue = true, allowableValues = "range[0, infinity]")
    private final long offset;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private final List<FilterInstance> filters;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private final Map<String, Consumer<StringBuilder>> sorters;

    public RequestPagination(Long limit, Long offset) {
        this.limit = ApiUtils.limitOrDefault(limit);
        this.offset = ApiUtils.offsetOrZero(offset);
        this.filters = new ArrayList<>();
        this.sorters = new LinkedHashMap<>();
    }

    public long getLimit() {
        return limit;
    }

    public long getOffset() {
        return offset;
    }

    public List<FilterInstance> getFilters() {
        return filters;
    }

    public Map<String, Consumer<StringBuilder>> getSorters() {
        return sorters;
    }

    @Override
    public String toString() {
        return "RequestPagination{" +
                "limit=" + limit +
                ", offset=" + offset +
                ", filters=" + filters +
                ", sorters=" + sorters.keySet() +
                '}';
    }
}
