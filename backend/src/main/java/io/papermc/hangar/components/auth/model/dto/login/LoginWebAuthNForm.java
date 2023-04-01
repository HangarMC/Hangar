package io.papermc.hangar.components.auth.model.dto.login;

public record LoginWebAuthNForm(String usernameOrEmail, String password, String publicKeyCredentialJson) {
}
