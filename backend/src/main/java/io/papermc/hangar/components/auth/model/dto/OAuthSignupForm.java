package io.papermc.hangar.components.auth.model.dto;

public record OAuthSignupForm(String username, String email, String jwt, boolean tos) {
}
