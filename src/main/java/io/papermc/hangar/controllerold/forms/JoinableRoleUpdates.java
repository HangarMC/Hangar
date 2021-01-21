package io.papermc.hangar.controllerold.forms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.controllerold.forms.objects.UserRole;

import java.util.List;

public class JoinableRoleUpdates {

    private final List<UserRole> updates;
    private final List<UserRole> additions;

    @JsonCreator
    public JoinableRoleUpdates(@JsonProperty(value = "updates", required = true) List<UserRole> updates, @JsonProperty(value = "additions", required = true) List<UserRole> additions) {
        this.updates = updates;
        this.additions = additions;
    }

    public List<UserRole> getUpdates() {
        return updates;
    }

    public List<UserRole> getAdditions() {
        return additions;
    }

    @Override
    public String toString() {
        return "OrganizationRoleUpdate{" +
                "updates=" + updates +
                ", additions=" + additions +
                '}';
    }
}
