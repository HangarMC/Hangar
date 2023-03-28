package io.papermc.hangar.components.auth.model.credential;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PasswordCredential(@JsonProperty("hashed_password") String hashedPassword) implements Credential{

    @Override
    public CredentialType type() {
        return CredentialType.PASSWORD;
    }
}
