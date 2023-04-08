package io.papermc.hangar.components.auth.model.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.StringUtils;

public record BackupCodeCredential(@JsonProperty("recovery_codes") List<BackupCode> backupCodes, boolean unconfirmed) implements Credential {
    @Override
    public CredentialType type() {
        return CredentialType.BACKUP_CODES;
    }

    public boolean matches(final String code) {
        if (!StringUtils.hasText(code)) return false;
        return this.backupCodes.stream().anyMatch(b -> b.code().equals(code));
    }

    public record BackupCode(String code, @Nullable @JsonProperty("used_at") OffsetDateTime usedAt) {}
}
