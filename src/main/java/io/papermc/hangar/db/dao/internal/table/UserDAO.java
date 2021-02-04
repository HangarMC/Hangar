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

@Repository
@RegisterConstructorMapper(UserTable.class)
public interface UserDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO users (id, created_at, full_name, name, email, tagline, join_date, read_prompts, locked, language) " +
               "VALUES (:id, :now, :fullName, :name, :email, :tagline, :now, :readPrompts, :locked, :language)")
    UserTable insert(@BindBean UserTable user);

    @GetGeneratedKeys
    @SqlUpdate("UPDATE users SET full_name = :fullName, name = :name, email = :email, tagline = :tagline, read_prompts = :readPrompts, locked = :locked, language = :language WHERE id = :id")
    UserTable update(@BindBean UserTable user);

    @SqlQuery("SELECT * FROM users WHERE id = :id OR name = :name")
    UserTable _getUserTable(Long id, String name);
    default UserTable getUserTable(long id) {
        return _getUserTable(id, null);
    }
    default UserTable getUserTable(@NotNull String name) {
        return _getUserTable(null, name);
    }

    @SqlQuery("SELECT * FROM users WHERE LOWER(name) = LOWER(:name)")
    UserTable getByName(String name);
}
