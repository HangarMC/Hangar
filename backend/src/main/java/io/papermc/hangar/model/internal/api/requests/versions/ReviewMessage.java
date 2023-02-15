package io.papermc.hangar.model.internal.api.requests.versions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotNull;

public record ReviewMessage(@NotNull String message, @NotNull ObjectNode args) {
}
