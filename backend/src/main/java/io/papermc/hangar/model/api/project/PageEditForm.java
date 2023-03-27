package io.papermc.hangar.model.api.project;

import jakarta.validation.constraints.NotNull;

public record PageEditForm(@NotNull String path, @NotNull String content) {
}
