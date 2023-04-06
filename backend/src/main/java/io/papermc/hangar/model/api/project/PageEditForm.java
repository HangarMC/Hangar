package io.papermc.hangar.model.api.project;

import jakarta.validation.constraints.NotNull;

public record PageEditForm(@NotNull String path, @NotNull String content) {

    public String path() {
        return this.path != null ? this.path : "";
    }

    public String content() {
        return this.content != null ? this.content : "";
    }
}
