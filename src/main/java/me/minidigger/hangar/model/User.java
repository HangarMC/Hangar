package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;
import org.threeten.bp.OffsetDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * ModelsProtocolsAPIV2User
 */
@Validated
public class User {
    @JsonProperty("created_at")
    private OffsetDateTime createdAt = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("tagline")
    private String tagline = null;

    @JsonProperty("join_date")
    private OffsetDateTime joinDate = null;

    @JsonProperty("roles")
    @Valid
    private List<Role> roles = new ArrayList<>();

    public User createdAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Get createdAt
     *
     * @return createdAt
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User name(String name) {
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

    public User tagline(String tagline) {
        this.tagline = tagline;
        return this;
    }

    /**
     * Get tagline
     *
     * @return tagline
     **/
    @ApiModelProperty(value = "")

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public User joinDate(OffsetDateTime joinDate) {
        this.joinDate = joinDate;
        return this;
    }

    /**
     * Get joinDate
     *
     * @return joinDate
     **/
    @ApiModelProperty(value = "")

    @Valid
    public OffsetDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(OffsetDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public User roles(List<Role> roles) {
        this.roles = roles;
        return this;
    }

    public User addRolesItem(Role rolesItem) {
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
        User user = (User) o;
        return Objects.equals(this.createdAt, user.createdAt) &&
               Objects.equals(this.name, user.name) &&
               Objects.equals(this.tagline, user.tagline) &&
               Objects.equals(this.joinDate, user.joinDate) &&
               Objects.equals(this.roles, user.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt, name, tagline, joinDate, roles);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelsProtocolsAPIV2User {\n");

        sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    tagline: ").append(toIndentedString(tagline)).append("\n");
        sb.append("    joinDate: ").append(toIndentedString(joinDate)).append("\n");
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
