package io.papermc.hangar.db.daoold;

import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.modelold.viewhelpers.FlagActivity;
import io.papermc.hangar.modelold.viewhelpers.ReviewActivity;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.BindList.EmptyHandling;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterBeanMapper(UsersTable.class)
public interface UserDao {

    @SqlUpdate("insert into users (id, created_at, full_name, name, email, tagline, join_date, read_prompts, is_locked, language) " +
               "values (:id, :now, :fullName, :name, :email, :tagline, :now, :readPrompts, :isLocked, :language)")
    @Timestamped
    @GetGeneratedKeys
    UsersTable insert(@BindBean UsersTable user);

    @SqlUpdate("UPDATE users SET full_name = :fullName, name = :name, email = :email, tagline = :tagline, read_prompts = :readPrompts, is_locked = :isLocked, language = :language WHERE id = :id")
    @GetGeneratedKeys
    UsersTable update(@BindBean UsersTable user);

    @SqlQuery("select * from users where id = :id")
    UsersTable getById(long id);

    @SqlQuery("select * from users where LOWER(name) = LOWER(:name)")
    UsersTable getByName(String name);

    @SqlQuery("SELECT * FROM users WHERE name IN (<userNames>)")
    List<UsersTable> getUsers(@BindList(onEmpty = EmptyHandling.NULL_STRING) List<String> userNames);

    @SqlQuery("SELECT u.* FROM project_watchers pw JOIN users u ON pw.user_id = u.id WHERE project_id = :projectId OFFSET :offset LIMIT :limit")
    List<UsersTable> getProjectWatchers(long projectId, int offset, Integer limit);

    @SqlUpdate("INSERT INTO project_watchers (project_id, user_id) VALUES (:projectId, :userId)")
    void setWatching(long projectId, long userId);

    @SqlUpdate("DELETE FROM project_watchers WHERE project_id = :projectId AND user_id = :userId")
    void removeWatching(long projectId, long userId);

    @SqlQuery("SELECT u.* FROM project_stars ps JOIN users u ON ps.user_id = u.id WHERE project_id = :projectId OFFSET :offset LIMIT :limit")
    List<UsersTable> getProjectStargazers(long projectId, int offset, Integer limit);

    @SqlUpdate("INSERT INTO project_stars (project_id, user_id) VALUES (:projectId, :userId)")
    void setStargazing(long projectId, long userId);

    @SqlUpdate("DELETE FROM project_stars WHERE project_id = :projectId AND user_id = :userId")
    void removeStargazing(long projectId, long userId);


    @SqlQuery("SELECT pvr.ended_at, pv.version_string, pv.version_string || '.' || pv.id AS version_string_url, p.owner_name \"owner\", p.slug" +
            "  FROM users u" +
            "         JOIN project_version_reviews pvr ON u.id = pvr.user_id" +
            "         JOIN project_versions pv ON pvr.version_id = pv.id" +
            "         JOIN projects p ON pv.project_id = p.id" +
            "  WHERE u.name = :username" +
            "  LIMIT 20")
    @RegisterBeanMapper(ReviewActivity.class)
    List<ReviewActivity> getReviewActivity(String username);

    @SqlQuery("SELECT pf.resolved_at, p.owner_name, p.slug" +
            "  FROM users u" +
            "         JOIN project_flags pf ON u.id = pf.user_id" +
            "         JOIN projects p ON pf.project_id = p.id" +
            "  WHERE u.name = :username" +
            "  LIMIT 20")
    @RegisterBeanMapper(FlagActivity.class)
    List<FlagActivity> getFlagActivity(String username);

    @SqlQuery("SELECT u.name" +
              "    FROM users u" +
              "    ORDER BY (SELECT COUNT(*) FROM project_members_all pma WHERE pma.user_id = u.id) DESC" +
              "    LIMIT 49000")
    List<String> getAllAuthorNames();
}
