package io.papermc.hangar.components.auth.model.db;

import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class VerificationCodeTable extends Table {

    private final long userId;
    private final VerificationCodeType type;
    private final String code;

    @JdbiConstructor
    public VerificationCodeTable(final long id, final OffsetDateTime createdAt, final long userId, @EnumByOrdinal final VerificationCodeType type, final String code) {
        super(createdAt, id);
        this.userId = userId;
        this.type = type;
        this.code = code;
    }

    public VerificationCodeTable(final long userId, final VerificationCodeType type, final String code) {
        this.userId = userId;
        this.type = type;
        this.code = code;
    }

    public long getUserId() {
        return this.userId;
    }

    @EnumByOrdinal
    public VerificationCodeType getType() {
        return this.type;
    }

    public String getCode() {
        return this.code;
    }

    public enum VerificationCodeType {
        EMAIL_VERIFICATION,
        PASSWORD_RESET
    }
}
