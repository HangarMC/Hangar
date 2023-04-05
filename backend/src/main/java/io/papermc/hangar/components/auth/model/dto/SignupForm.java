package io.papermc.hangar.components.auth.model.dto;

public record SignupForm(String username, String email, String password, boolean tos) {
}
