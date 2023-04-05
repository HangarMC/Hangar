package io.papermc.hangar.components.auth.model.dto.login;

import io.papermc.hangar.components.auth.model.credential.CredentialType;
import java.util.List;

public record LoginResponse(int aal, List<CredentialType> types) {
}
