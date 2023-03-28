package io.papermc.hangar.components.auth.model.db;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.components.auth.model.credential.CredentialType;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByOrdinal;

public class UserCredentialTable extends Table {

    private final long userId;
    private final JSONB credential;
    private final CredentialType type;

    public UserCredentialTable(final long id, final OffsetDateTime createdAt, final long userId, final JSONB credential, @EnumByOrdinal final CredentialType type) {
        super( createdAt, id);
        this.userId = userId;
        this.credential = credential;
        this.type = type;
    }

    public long getUserId() {
        return this.userId;
    }

    public JSONB getCredential() {
        return this.credential;
    }

    public CredentialType getType() {
        return this.type;
    }
}
