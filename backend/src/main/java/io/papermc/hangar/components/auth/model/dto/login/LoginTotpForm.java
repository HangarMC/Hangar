package io.papermc.hangar.components.auth.model.dto.login;

public record LoginTotpForm(String usernameOrEmail, String password, String totpCode) {
}
