package io.papermc.hangar.modelold.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * ControllersApiv2ApiV2ControllerPaginatedCompactProjectResult
 */
@Validated
public class PaginatedCompactProjectResult {
    @JsonProperty("pagination")
    private Pagination pagination = null;

    @JsonProperty("result")
    @Valid
    private List<ProjectCompact> result = new ArrayList<>();

    public PaginatedCompactProjectResult pagination(Pagination pagination) {
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

    public PaginatedCompactProjectResult result(List<ProjectCompact> result) {
        this.result = result;
        return this;
    }

    public PaginatedCompactProjectResult addResultItem(ProjectCompact resultItem) {
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
    public List<ProjectCompact> getResult() {
        return result;
    }

    public void setResult(List<ProjectCompact> result) {
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
        PaginatedCompactProjectResult controllersApiv2ApiV2ControllerPaginatedCompactProjectResult = (PaginatedCompactProjectResult) o;
        return Objects.equals(this.pagination, controllersApiv2ApiV2ControllerPaginatedCompactProjectResult.pagination) &&
               Objects.equals(this.result, controllersApiv2ApiV2ControllerPaginatedCompactProjectResult.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pagination, result);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ControllersApiv2ApiV2ControllerPaginatedCompactProjectResult {\n");

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
