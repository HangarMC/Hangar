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
    @SqlUpdate("INSERT INTO users (uuid, created_at, full_name, name, email, tagline, join_date, read_prompts, locked, language, theme) " +
               "VALUES (:uuid, :now, :fullName, :name, :email, :tagline, :now, :readPrompts, :locked, :language, :theme)")
    UserTable insert(@BindBean UserTable user);

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO users (uuid, created_at, full_name, name, email, tagline, join_date, read_prompts, locked, language, theme) " +
               "VALUES (:uuid, :now, :fullName, :name, :email, :tagline, :now, :readPrompts, :locked, :language, :theme)")
    UserTable create(UUID uuid, String name, String email, String fullName, String tagline, String language, List<Integer> readPrompts, boolean locked, String theme);

    @GetGeneratedKeys
    @SqlUpdate("UPDATE users SET full_name = :fullName, name = :name, email = :email, tagline = :tagline, read_prompts = :readPrompts, locked = :locked, language = :language, theme = :theme WHERE id = :id")
    UserTable update(@BindBean UserTable user);

    @SqlQuery("SELECT * FROM users WHERE id = :id OR lower(name) = lower(:name)")
    UserTable _getUserTable(Long id, String name);
    default UserTable getUserTable(long id) {
        return _getUserTable(id, null);
    }
    default UserTable getUserTable(@NotNull String name) {
        return _getUserTable(null, name);
    }

    @SqlQuery("SELECT * FROM users WHERE email = :email")
    UserTable getUserTableByEmail(String email);

    @SqlQuery("SELECT u.name" +
            "    FROM users u" +
            "    ORDER BY (SELECT count(*) FROM project_members_all pma WHERE pma.user_id = u.id) DESC" +
            "    LIMIT 49000")
    List<String> getAuthorNames();
}
