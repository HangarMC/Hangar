package io.papermc.hangar.controllerold.forms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.NotNull;

public class UserAdminForm {
    private final String thing;
    private final String action;
    private final ObjectNode data;

    @JsonCreator(mode = Mode.PROPERTIES)
    public UserAdminForm(@JsonProperty(value = "thing", required = true) @NotNull String thing,
                         @JsonProperty(value = "action", required = true) @NotNull String action,
                         @JsonProperty(value = "data", required = true) @NotNull ObjectNode data) {
        this.thing = thing;
        this.action = action;
        this.data = data;
    }

    public String getThing() {
        return thing;
    }

    public String getAction() {
        return action;
    }

    public ObjectNode getData() {
        return data;
    }

    @Override
    public String toString() {
        return "UserAdminForm{" +
                "thing='" + thing + '\'' +
                ", action='" + action + '\'' +
                ", data=" + data +
                '}';
    }
}
