package io.papermc.hangar.components.auth.model.dto;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import org.jetbrains.annotations.Nullable;

public record WebAuthNSetupResponse(
    String username,
    @Nullable String credentialNickname,
    ByteArray requestId,
    PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions
) {
}
