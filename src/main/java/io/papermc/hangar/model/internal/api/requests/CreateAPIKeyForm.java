package io.papermc.hangar.model.internal.api.requests;

import io.papermc.hangar.model.common.NamedPermission;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

public class CreateAPIKeyForm {

    @NotBlank
    @Size(min = 5, max = 255)
    @ApiModelProperty(allowableValues = "range[5,256)", required = true)
    private final String name;

    @Size(min = 1)
    // TODO this shows up all wonky in the api docs...
    @ApiModelProperty(dataType = "List", required = true)
    private final List<NamedPermission> permissions;

    public CreateAPIKeyForm(String name, List<NamedPermission> permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    public String getName() {
        return name;
    }

    public List<NamedPermission> getPermissions() {
        return permissions;
    }

    @Override
    public String toString() {
        return "CreateAPIKeyForm{" +
                "name='" + name + '\'' +
                ", permissions=" + permissions +
                '}';
    }
}
