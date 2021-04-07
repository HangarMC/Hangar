package io.papermc.hangar.db.daoold;

import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.modelold.viewhelpers.FlagActivity;
import io.papermc.hangar.modelold.viewhelpers.ReviewActivity;
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


    @SqlQuery("SELECT pvr.ended_at, pv.version_string, pv.version_string || '.' || pv.id AS version_string_url, p.owner_name \"owner\", p.slug" +
            "  FROM users u" +
            "         JOIN project_version_reviews pvr ON u.id = pvr.user_id" +
            "         JOIN project_versions pv ON pvr.version_id = pv.id" +
            "         JOIN projects p ON pv.project_id = p.id" +
            "  WHERE u.name = :username" +
            "  LIMIT 20")
    @RegisterBeanMapper(ReviewActivity.class)
    List<ReviewActivity> getReviewActivity(String usernafme);

    @SqlQuery("SELECT pf.resolved_at, p.owner_name, p.slug" +
            "  FROM users u" +
            "         JOIN project_flags pf ON u.id = pf.user_id" +
            "         JOIN projects p ON pf.project_id = p.id" +
            "  WHERE u.name = :username" +
            "  LIMIT 20")
    @RegisterBeanMapper(FlagActivity.class)
    List<FlagActivity> getFlagActivity(String username);

}
