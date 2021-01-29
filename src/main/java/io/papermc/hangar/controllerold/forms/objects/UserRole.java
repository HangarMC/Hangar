package io.papermc.hangar.controllerold.forms.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.modelold.Role;
import org.jetbrains.annotations.NotNull;

public class UserRole {

    private final Role role;
    private final long userId;

    @JsonCreator
    public UserRole(@NotNull @JsonProperty(value = "role", required = true) Role role, @JsonProperty(value = "id", required = true) long userId) {
        this.role = role;
        this.userId = userId;
    }

    public Role getRole() {
        return role;
    }

    public long getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "UserRole{" +
                "role=" + role +
                ", userId=" + userId +
                '}';
    }
}
