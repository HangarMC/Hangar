package io.papermc.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.model.Role;
import io.swagger.annotations.ApiModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Validated
public class User {
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("tagline")
    private String tagline = null;

    @JsonProperty("joinDate")
    private OffsetDateTime joinDate = null;

    @JsonProperty("roles")
    private List<Role> roles = new ArrayList<>();

    @ApiModelProperty(required = true)
    @NotNull
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @ApiModelProperty(required = true)
    @NotNull
    @Valid
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty()
    @Nullable
    @Valid
    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    @ApiModelProperty()
    @Nullable
    @Valid
    public OffsetDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(OffsetDateTime joinDate) {
        this.joinDate = joinDate;
    }

    @ApiModelProperty(required = true)
    @NotNull
    @Valid
    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "User{" +
                "createdAt=" + createdAt +
                ", name='" + name + '\'' +
                ", tagline='" + tagline + '\'' +
                ", joinDate=" + joinDate +
                ", roles=" + roles +
                '}';
    }
}
