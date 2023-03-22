package io.papermc.hangar.model.internal.api.requests.projects;

import io.papermc.hangar.model.common.projects.Visibility;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VisibilityChangeForm(@NotNull Visibility visibility, @Size(max = 500) String comment) {
}
