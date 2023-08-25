package io.papermc.hangar.model.api.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApiSession(
    @Schema(description = "JWT used for authentication") String token,
    @Schema(description = "Milliseconds this JWT expires in") long expiresIn
) {
}
