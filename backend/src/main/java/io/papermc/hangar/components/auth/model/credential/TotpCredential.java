package io.papermc.hangar.components.auth.model.credential;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TotpCredential(@JsonProperty("totp_url") String totpUrl) implements Credential{
    @Override
    public CredentialType type() {
        return CredentialType.TOTP;
    }
}
