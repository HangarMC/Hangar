package io.papermc.hangar.model.api.requests;

import io.papermc.hangar.model.common.projects.FlagReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FlagForm(@NotBlank String comment, long projectId, @NotNull FlagReason reason) {
}
