package io.papermc.hangar.components.auth.model.dto;

import java.util.List;

public record SettingsResponse(List<Authenticator> authenticators, boolean hasBackupCodes, boolean hasTotp, boolean emailConfirmed, boolean emailPending) {

    public record Authenticator(String addedAt, String displayName, String id) {}
}
