package io.papermc.hangar.db.dao.internal.table;

import io.papermc.hangar.model.db.UserTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RegisterConstructorMapper(UserTable.class)
public interface UserDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("""
        INSERT INTO users (uuid, created_at, name, email, tagline, join_date, read_prompts, locked, language, theme)
        VALUES (:uuid, :now, :name, :email, :tagline, :now, :readPrompts, :locked, :language, :theme)
        """)
    UserTable insert(@BindBean UserTable user);

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("""
        INSERT INTO users (uuid, created_at, name, email, tagline, join_date, read_prompts, locked, language, theme)
        VALUES (:uuid, :now, :name, :email, :tagline, :now, :readPrompts, :locked, :language, :theme)
        """)
    UserTable create(UUID uuid, String name, String email, String tagline, String language, List<Integer> readPrompts, boolean locked, String theme);

    @SqlUpdate("DELETE FROM users WHERE id = :id")
    void delete(long id);

    @GetGeneratedKeys
    @SqlUpdate("UPDATE users SET name = :name, email = :email, tagline = :tagline, read_prompts = :readPrompts, locked = :locked, language = :language, theme = :theme WHERE id = :id")
    UserTable update(@BindBean UserTable user);

    @SqlQuery("SELECT * FROM users WHERE id = :id OR LOWER(name) = LOWER(:name) OR uuid = :uuid")
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

    @SqlQuery("SELECT * FROM users WHERE email = :email")
    UserTable getUserTableByEmail(String email);

    @SqlQuery("""
        SELECT u.name
        FROM users u
        ORDER BY (SELECT COUNT(*) FROM project_members_all pma WHERE pma.user_id = u.id) DESC
        LIMIT 49000
        """)
    List<String> getAuthorNames();
}
