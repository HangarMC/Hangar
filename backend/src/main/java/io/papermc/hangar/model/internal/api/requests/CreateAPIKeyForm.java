package io.papermc.hangar.model.internal.api.requests;

import io.papermc.hangar.model.common.NamedPermission;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CreateAPIKeyForm {

    @ApiModelProperty(allowableValues = "range[5,256)", required = true)
    private final @NotBlank @Size(min = 5, max = 255) String name;

    @ApiModelProperty(required = true)
    private final @Size(min = 1) List<NamedPermission> permissions;

    public CreateAPIKeyForm(final String name, final List<NamedPermission> permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    public String getName() {
        return this.name;
    }

    public List<NamedPermission> getPermissions() {
        return this.permissions;
    }

    @Override
    public String toString() {
        return "CreateAPIKeyForm{" +
            "name='" + this.name + '\'' +
            ", permissions=" + this.permissions +
            '}';
    }
}
