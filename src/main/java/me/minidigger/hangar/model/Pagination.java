package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * ControllersApiv2ApiV2ControllerPagination
 */
@Validated
public class Pagination {
    @JsonProperty("limit")
    private Long limit = null;

    @JsonProperty("offset")
    private Long offset = null;

    @JsonProperty("count")
    private Long count = null;

    public Pagination limit(Long limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Get limit
     *
     * @return limit
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public Pagination offset(Long offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Get offset
     *
     * @return offset
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public Pagination count(Long count) {
        this.count = count;
        return this;
    }

    /**
     * Get count
     *
     * @return count
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pagination pagination = (Pagination) o;
        return Objects.equals(this.limit, pagination.limit) &&
               Objects.equals(this.offset, pagination.offset) &&
               Objects.equals(this.count, pagination.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(limit, offset, count);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ControllersApiv2ApiV2ControllerPagination {\n");

        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
