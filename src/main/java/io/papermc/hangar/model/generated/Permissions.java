package io.papermc.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.papermc.hangar.model.NamedPermission;
import io.papermc.hangar.model.PermissionType;
import io.swagger.annotations.ApiModelProperty;

/**
 * ControllersApiv2ApiV2ControllerKeyPermissions
 */
@Validated
public class Permissions {

    @JsonProperty("type")
    private PermissionType type = null;

    @JsonProperty("permissions")
    @Valid
    private List<NamedPermission> permissions = new ArrayList<>();

    public Permissions type(PermissionType type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     *
     * @return type
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public PermissionType getType() {
        return type;
    }

    public void setType(PermissionType type) {
        this.type = type;
    }

    public Permissions permissions(List<NamedPermission> permissions) {
        this.permissions = permissions;
        return this;
    }

    public Permissions addPermissionsItem(NamedPermission permissionsItem) {
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
    @Valid
    public List<NamedPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<NamedPermission> permissions) {
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
        Permissions controllersApiv2ApiV2ControllerKeyPermissions = (Permissions) o;
        return Objects.equals(this.type, controllersApiv2ApiV2ControllerKeyPermissions.type) &&
               Objects.equals(this.permissions, controllersApiv2ApiV2ControllerKeyPermissions.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, permissions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ControllersApiv2ApiV2ControllerKeyPermissions {\n");

        sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
