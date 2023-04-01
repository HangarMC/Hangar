package io.papermc.hangar.components.auth.model.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public record WebAuthNCredential(@JsonProperty("user_handle") String userHandle, List<WebAuthNDevice> credentials, @Nullable PendingSetup pendingSetup, @Nullable PendingLogin pendingLogin) implements Credential{
    @Override
    public CredentialType type() {
        return CredentialType.WEBAUTHN;
    }

    public record WebAuthNDevice(
        String id,
        @JsonProperty("added_at") String addedAt,
        @JsonProperty("public_key") String publicKey,
        @JsonProperty("display_name") String displayName,
        Authenticator authenticator,
        @JsonProperty("is_passwordless") boolean isPasswordLess,
        @JsonProperty("attestation_type") String attestationType
    ) {}

    public record Authenticator(String aaguid, @JsonProperty("sign_count") long signCount, @JsonProperty("clone_warning") boolean cloneWarning) {}

    public record PendingSetup(String authenticatorName, String json) {}
    public record PendingLogin(String json) {}
}
