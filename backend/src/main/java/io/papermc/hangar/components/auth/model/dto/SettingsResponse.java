package io.papermc.hangar.components.auth.model.dto;

public record SettingsResponse(boolean hasBackupCodes, boolean hasTotp, boolean emailConfirmed, boolean emailPending) {
}
