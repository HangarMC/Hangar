package io.papermc.hangar.components.auth.model.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public record BackupCodeCredential(@JsonProperty("recovery_codes") List<BackupCode> backupCodes, boolean unconfirmed) implements Credential{
    @Override
    public CredentialType type() {
        return CredentialType.BACKUP_CODES;
    }

    public record BackupCode(String code, @Nullable @JsonProperty("used_at") OffsetDateTime usedAt) {}
}
