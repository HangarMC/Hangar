package io.papermc.hangar.components.auth.model.dto.webauthn;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record AuthenticatorForm(Integer id,
                                String credentialId,
                                @NotEmpty String name,
                                CollectedClientDataForm clientData,
                                AttestationObjectForm attestationObject,
                                Set<String> transports,
                                String clientExtensionsJSON) {
}
