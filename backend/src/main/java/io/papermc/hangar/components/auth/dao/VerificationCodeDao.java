package io.papermc.hangar.components.auth.dao;

import io.papermc.hangar.components.auth.model.db.VerificationCodeTable;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(VerificationCodeTable.class)
public interface VerificationCodeDao {

    @Timestamped
    @SqlUpdate("INSERT INTO verification_codes(user_id, created_at, type, code) VALUES (:userId, :now, :type, :code)")
    void insert(@BindBean VerificationCodeTable table);

    @SqlQuery("SELECT * FROM verification_codes WHERE type = :type AND user_id = :userId")
    @Nullable
    VerificationCodeTable get(@EnumByOrdinal VerificationCodeTable.VerificationCodeType type, long userId);

    @SqlUpdate("DELETE FROM verification_codes WHERE id = :id")
    void delete(long id);

    @SqlUpdate("DELETE FROM verification_codes WHERE type = :type AND user_id = :userId")
    void deleteOld(@EnumByOrdinal VerificationCodeTable.VerificationCodeType type, long userId);
}
