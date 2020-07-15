package me.minidigger.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * ControllersApiv2ApiV2ControllerPaginatedVersionResult
 */
@Validated
public class PaginatedVersionResult {
    @JsonProperty("pagination")
    private Pagination pagination = null;

    @JsonProperty("result")
    @Valid
    private List<Version> result = new ArrayList<>();

    public PaginatedVersionResult pagination(Pagination pagination) {
        this.pagination = pagination;
        return this;
    }

    /**
     * Get pagination
     *
     * @return pagination
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid
    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public PaginatedVersionResult result(List<Version> result) {
        this.result = result;
        return this;
    }

    public PaginatedVersionResult addResultItem(Version resultItem) {
        this.result.add(resultItem);
        return this;
    }

    /**
     * Get result
     *
     * @return result
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    @Valid
    public List<Version> getResult() {
        return result;
    }

    public void setResult(List<Version> result) {
        this.result = result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PaginatedVersionResult paginatedVersionResult = (PaginatedVersionResult) o;
        return Objects.equals(this.pagination, paginatedVersionResult.pagination) &&
               Objects.equals(this.result, paginatedVersionResult.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pagination, result);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ControllersApiv2ApiV2ControllerPaginatedVersionResult {\n");

        sb.append("    pagination: ").append(toIndentedString(pagination)).append("\n");
        sb.append("    result: ").append(toIndentedString(result)).append("\n");
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
