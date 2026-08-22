package io.papermc.hangar.model.internal.api.requests.admin;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

public record ChangeRoleForm(long roleId, @NotBlank String title, @NotBlank String color, @Nullable Integer rank) {
}
