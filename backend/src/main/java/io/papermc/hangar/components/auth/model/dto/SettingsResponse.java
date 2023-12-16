package io.papermc.hangar.components.auth.model.dto;

import io.papermc.hangar.components.auth.model.credential.OAuthCredential;
import java.util.List;

public record SettingsResponse(List<Authenticator> authenticators, List<OAuthCredential.OAuthConnection> oauthConnections, boolean hasBackupCodes, boolean hasTotp, boolean emailConfirmed, boolean emailPending, boolean hasPassword) {

    public record Authenticator(String addedAt, String displayName, String id) {}
}
