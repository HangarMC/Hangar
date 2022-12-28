package io.papermc.hangar.model.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.controller.extras.pagination.Filter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

public class RequestPagination {

    @Schema(description = "The maximum amount of items to return", example = "1", requiredMode = NOT_REQUIRED, allowableValues = "range[1, 25]")
    private final long limit;

    @Schema(description = "Where to start searching", example = "0", requiredMode = NOT_REQUIRED, allowableValues = "range[0, infinity]")
    private final long offset;

    @JsonIgnore
    @Schema(accessMode = READ_ONLY)
    private final List<Filter.FilterInstance> filters;

    @JsonIgnore
    @Schema(accessMode = READ_ONLY)
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
