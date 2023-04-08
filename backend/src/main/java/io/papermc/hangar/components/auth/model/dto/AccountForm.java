package io.papermc.hangar.components.auth.model.dto;

import jakarta.validation.constraints.NotEmpty;

public record AccountForm(@NotEmpty String username, @NotEmpty String email, @NotEmpty String currentPassword, String newPassword) {
}
