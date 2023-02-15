package io.papermc.hangar.model.internal.api.requests.projects;

import io.papermc.hangar.model.common.projects.Visibility;
import jakarta.validation.constraints.NotNull;

public record VisibilityChangeForm(@NotNull Visibility visibility, String comment) {
}
