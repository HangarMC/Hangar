package io.papermc.hangar.model.api.auth;

public record ApiSession(String token, long expiresIn) {
}
