package io.papermc.hangar.components.auth.model.dto.login;

public record LoginBackupForm(String usernameOrEmail, String password, String backupCode) {
}
