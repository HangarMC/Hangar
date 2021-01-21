package io.papermc.hangar.modelold.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.modelold.generated.Pagination;
import io.papermc.hangar.modelold.generated.User;
import io.swagger.annotations.ApiModelProperty;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;

@Validated
public class PaginatedUserResult {

    @JsonProperty("pagination")
    private final Pagination pagination;

    @Valid
    @JsonProperty("result")
    private final List<User> result;

    public PaginatedUserResult(Pagination pagination, @Valid List<User> result) {
        this.pagination = pagination;
        this.result = result;
    }

    @Valid
    @NotNull
    @ApiModelProperty(required = true, value = "Pagination information")
    public Pagination getPagination() {
        return pagination;
    }


    @Valid
    @NotNull
    @ApiModelProperty(required = true, value = "List of users")
    public List<User> getResult() {
        return result;
    }
}
