package io.papermc.hangar.model.internal.api.requests.admin;

import jakarta.validation.constraints.NotBlank;
import org.checkerframework.checker.nullness.qual.Nullable;

public record ChangeRoleForm(long roleId, @NotBlank String title, @NotBlank String color, @Nullable Integer rank) {
}
