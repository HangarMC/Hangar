package io.papermc.hangar.model.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.controller.extras.pagination.Filter;
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
    private final List<Filter.FilterInstance> filters;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private final Map<String, Consumer<StringBuilder>> sorters;

    /**
     * limit/offset params should be validated before construction
     */
    public RequestPagination(final Long limit, final Long offset) {
        this.limit = limit;
        this.offset = offset;
        this.filters = new ArrayList<>();
        this.sorters = new LinkedHashMap<>();
    }

    public long getLimit() {
        return this.limit;
    }

    public long getOffset() {
        return this.offset;
    }

    public List<Filter.FilterInstance> getFilters() {
        return this.filters;
    }

    public Map<String, Consumer<StringBuilder>> getSorters() {
        return this.sorters;
    }

    @Override
    public String toString() {
        return "RequestPagination{" +
                "limit=" + this.limit +
                ", offset=" + this.offset +
                ", filters=" + this.filters +
                ", sorters=" + this.sorters.keySet() +
                '}';
    }
}
