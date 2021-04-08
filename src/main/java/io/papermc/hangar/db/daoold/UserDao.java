package io.papermc.hangar.db.daoold;

import io.papermc.hangar.db.modelold.UsersTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Deprecated(forRemoval = true)
@RegisterBeanMapper(UsersTable.class)
public interface UserDao {

    @SqlUpdate("UPDATE users SET full_name = :fullName, name = :name, email = :email, tagline = :tagline, read_prompts = :readPrompts, is_locked = :isLocked, language = :language WHERE id = :id")
    @GetGeneratedKeys
    UsersTable update(@BindBean UsersTable user);

    @SqlQuery("select * from users where id = :id")
    UsersTable getById(long id);

    @SqlQuery("select * from users where LOWER(name) = LOWER(:name)")
    UsersTable getByName(String name);

    @SqlQuery("SELECT u.* FROM project_watchers pw JOIN users u ON pw.user_id = u.id WHERE project_id = :projectId OFFSET :offset LIMIT :limit")
    List<UsersTable> getProjectWatchers(long projectId, int offset, Integer limit);

    @SqlQuery("SELECT u.* FROM project_stars ps JOIN users u ON ps.user_id = u.id WHERE project_id = :projectId OFFSET :offset LIMIT :limit")
    List<UsersTable> getProjectStargazers(long projectId, int offset, Integer limit);
}
