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
 * ControllersApiv2ApiV2ControllerKeyToCreate
 */
@Validated
public class ApiKeyRequest {
    @JsonProperty("name")
    private String name = null;

    @JsonProperty("permissions")
    @Valid
    private List<String> permissions = new ArrayList<>();

    public ApiKeyRequest name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
     *
     * @return name
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApiKeyRequest permissions(List<String> permissions) {
        this.permissions = permissions;
        return this;
    }

    public ApiKeyRequest addPermissionsItem(String permissionsItem) {
        this.permissions.add(permissionsItem);
        return this;
    }

    /**
     * Get permissions
     *
     * @return permissions
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiKeyRequest apiKeyRequest = (ApiKeyRequest) o;
        return Objects.equals(this.name, apiKeyRequest.name) &&
               Objects.equals(this.permissions, apiKeyRequest.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, permissions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ControllersApiv2ApiV2ControllerKeyToCreate {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    permissions: ").append(toIndentedString(permissions)).append("\n");
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
