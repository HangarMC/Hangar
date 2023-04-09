package io.papermc.hangar.components.auth.model.dto.login;

import io.papermc.hangar.components.auth.model.credential.CredentialType;
import io.papermc.hangar.model.internal.user.HangarUser;
import java.util.List;

public record LoginResponse(int aal, List<CredentialType> types, HangarUser user) {
}
