package io.papermc.hangar.components.auth.model.dto;

import jakarta.validation.constraints.NotEmpty;

public record ResetForm(@NotEmpty String email, String code, String password) {
}
