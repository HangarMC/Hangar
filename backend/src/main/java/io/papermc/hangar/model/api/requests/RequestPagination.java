package io.papermc.hangar.model.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.controller.extras.pagination.Filter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

public class RequestPagination {

    @Schema(description = "The maximum amount of items to return", example = "1", requiredMode = NOT_REQUIRED, minimum = "1", maximum = "25")
    private final long limit;

    @Schema(description = "Where to start searching", example = "0", requiredMode = NOT_REQUIRED, minimum = "0")
    private final long offset;

    @JsonIgnore
    @Schema(accessMode = READ_ONLY)
    private final Map<String, Filter.FilterInstance> filters;

    @JsonIgnore
    @Schema(accessMode = READ_ONLY)
    private final Map<String, Consumer<StringBuilder>> sorters;

    /**
     * limit/offset params should be validated before construction
     */
    public RequestPagination(final Long limit, final Long offset) {
        this.limit = limit;
        this.offset = offset;
        this.filters = new LinkedHashMap<>();
        this.sorters = new LinkedHashMap<>();
    }

    public long getLimit() {
        return this.limit;
    }

    public long getOffset() {
        return this.offset;
    }

    public Map<String, Filter.FilterInstance> getFilters() {
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
            ", filters=" + this.filters.keySet() +
            ", sorters=" + this.sorters.keySet() +
            '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final RequestPagination that = (RequestPagination) o;
        return this.limit == that.limit && this.offset == that.offset && Objects.equals(this.filters.keySet(), that.filters.keySet()) && Objects.equals(this.sorters.keySet(), that.sorters.keySet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.limit, this.offset, this.filters.keySet(), this.sorters.keySet());
    }
}
