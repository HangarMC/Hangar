package io.papermc.hangar.components.auth.model.dto;

import io.papermc.hangar.components.auth.model.credential.OAuthCredential;
import java.time.OffsetDateTime;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record SettingsResponse(List<Authenticator> authenticators, List<OAuthCredential.OAuthConnection> oauthConnections, boolean hasBackupCodes, boolean hasTotp, boolean emailConfirmed, boolean emailPending, boolean hasPassword, @Nullable OffsetDateTime deletionScheduledFor, long ownedProjectCount, long ownedOrganizationCount) {

    public record Authenticator(String addedAt, String displayName, String id) {}
}
