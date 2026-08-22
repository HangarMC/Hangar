package io.papermc.hangar.components.auth.model.dto;

import com.yubico.webauthn.data.ByteArray;
import org.jspecify.annotations.Nullable;

public record WebAuthNForm(
    @Nullable String credentialName,
    @Nullable ByteArray sessionToken
) {
}
