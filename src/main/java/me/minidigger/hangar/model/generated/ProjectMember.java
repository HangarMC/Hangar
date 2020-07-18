package me.minidigger.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import me.minidigger.hangar.model.Role;

/**
 * ModelsProtocolsAPIV2ProjectMember
 */
@Validated
public class ProjectMember {
    @JsonProperty("user")
    private String user = null;

    @JsonProperty("roles")
    @Valid
    private List<Role> roles = new ArrayList<>();

    public ProjectMember user(String user) {
        this.user = user;
        return this;
    }

    /**
     * Get user
     *
     * @return user
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public ProjectMember roles(List<Role> roles) {
        this.roles = roles;
        return this;
    }

    public ProjectMember addRolesItem(Role rolesItem) {
        this.roles.add(rolesItem);
        return this;
    }

    /**
     * Get roles
     *
     * @return roles
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    @Valid
    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProjectMember projectMember = (ProjectMember) o;
        return Objects.equals(this.user, projectMember.user) &&
               Objects.equals(this.roles, projectMember.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, roles);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelsProtocolsAPIV2ProjectMember {\n");

        sb.append("    user: ").append(toIndentedString(user)).append("\n");
        sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
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
