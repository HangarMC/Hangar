package io.papermc.hangar.components.auth.dao;

import io.papermc.hangar.components.auth.model.AccountDeletionStatus;
import io.papermc.hangar.components.auth.model.PendingAccountDeletion;
import java.time.OffsetDateTime;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
public interface AccountDeletionDAO {

    @RegisterConstructorMapper(AccountDeletionStatus.class)
    @SqlQuery("""
        SELECT u.deletion_requested_at,
               (SELECT count(*) FROM projects p WHERE p.owner_id = u.id) AS owned_project_count,
               (SELECT count(*) FROM organizations o WHERE o.owner_id = u.id) AS owned_organization_count
        FROM users u
        WHERE u.id = :userId
        """)
    AccountDeletionStatus getStatus(long userId);

    @SqlQuery("UPDATE users SET deletion_requested_at = now() WHERE id = :userId AND deletion_requested_at IS NULL RETURNING deletion_requested_at")
    @Nullable OffsetDateTime requestDeletion(long userId);

    @SqlUpdate("UPDATE users SET deletion_requested_at = NULL WHERE id = :userId AND deletion_requested_at IS NOT NULL")
    int cancelDeletion(long userId);

    @SqlQuery("""
        SELECT id
        FROM users
        WHERE deletion_requested_at <= :cutoff
        """)
    List<Long> getDueAccountIds(OffsetDateTime cutoff);

    @RegisterConstructorMapper(PendingAccountDeletion.class)
    @SqlQuery("""
        SELECT id, uuid, name, email
        FROM users
        WHERE id = :userId AND deletion_requested_at <= :cutoff
        FOR UPDATE
        """)
    @Nullable PendingAccountDeletion getDueAccount(long userId, OffsetDateTime cutoff);
}
