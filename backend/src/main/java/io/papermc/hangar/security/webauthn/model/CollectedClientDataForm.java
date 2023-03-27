package io.papermc.hangar.security.webauthn.model;

import com.webauthn4j.data.client.CollectedClientData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CollectedClientDataForm(@NotNull @Valid CollectedClientData collectedClientData, @NotNull String clientDataBase64) {

    @Override
    public String toString() {
        return "CollectedClientDataForm{" +
            "collectedClientData=" + this.collectedClientData +
            '}';
    }
}
