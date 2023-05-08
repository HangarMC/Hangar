package io.papermc.hangar.db.dao.internal.table;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.api.UserNameChange;
import io.papermc.hangar.model.db.UserTable;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(UserTable.class)
public interface UserDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("""
        INSERT INTO users (uuid, created_at, name, email, tagline, read_prompts, locked, language, theme, email_verified, socials)
        VALUES (:uuid, :now, :name, :email, :tagline, :readPrompts, :locked, :language, :theme, :emailVerified, :socials)
        """)
    UserTable insert(@BindBean UserTable user);

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("""
        INSERT INTO users (uuid, created_at, name, email, tagline, read_prompts, locked, language, theme, email_verified, socials)
        VALUES (:uuid, :now, :name, :email, :tagline, :readPrompts, :locked, :language, :theme, :emailVerified, :socials)
        """)
    UserTable create(UUID uuid, String name, String email, String tagline, String language, List<Integer> readPrompts, boolean locked, String theme, boolean emailVerified, JSONB socials);

    @SqlUpdate("DELETE FROM users WHERE id = :id")
    void delete(long id);

    @GetGeneratedKeys
    @SqlUpdate("UPDATE users SET name = :name, email = :email, tagline = :tagline, read_prompts = :readPrompts, locked = :locked, language = :language, theme = :theme, email_verified = :emailVerified, socials = :socials WHERE id = :id")
    UserTable update(@BindBean UserTable user);

    @SqlUpdate("DELETE FROM users WHERE id = :id")
    void delete(@BindBean UserTable user);

    @SqlQuery("SELECT * FROM users WHERE id = :id OR lower(name) = lower(:name) OR lower(email) = lower(:name) OR uuid = :uuid")
    UserTable _getUserTable(Long id, String name, UUID uuid);

    default UserTable getUserTable(final long id) {
        return this._getUserTable(id, null, null);
    }

    default UserTable getUserTable(final @NotNull String name) {
        return this._getUserTable(null, name, null);
    }

    default UserTable getUserTable(final @NotNull UUID uuid) {
        return this._getUserTable(null, null, uuid);
    }

    @SqlQuery("""
        SELECT u.name
        FROM users u
        ORDER BY (SELECT count(*) FROM project_members_all pma WHERE pma.user_id = u.id) DESC
        LIMIT 49000
        """)
    List<String> getAuthorNames();

    @RegisterConstructorMapper(UserNameChange.class)
    @SqlQuery("SELECT old_name, new_name, date FROM users_history WHERE uuid = :uuid ORDER BY date ASC")
    List<UserNameChange> getUserNameHistory(final @NotNull UUID uuid);

    @Timestamped
    @SqlUpdate("INSERT INTO users_history(uuid, old_name, new_name, date) VALUES (:uuid, :oldName, :newName, :now)")
    void recordNameChange(final @NotNull UUID uuid, final @NotNull String oldName, final @NotNull String newName);
}
