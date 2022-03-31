package io.papermc.hangar.db.dao;

import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.viewhelpers.Author;
import io.papermc.hangar.model.viewhelpers.FlagActivity;
import io.papermc.hangar.model.viewhelpers.ReviewActivity;
import io.papermc.hangar.model.viewhelpers.Staff;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.BindList.EmptyHandling;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
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

    @SqlQuery("SELECT sq.name," +
              "       sq.join_date," +
              "       sq.created_at," +
              "       sq.role," +
              "       sq.donator_role," +
              "       sq.count" +
              "    FROM (SELECT u.name," +
              "                 u.join_date," +
              "                 u.created_at," +
              "                 r.name                                                                                           AS role," +
              "                 r.permission," +
              "                 (SELECT COUNT(*)" +
              "                      FROM project_members_all pma" +
              "                      WHERE pma.user_id = u.id)                                                                   AS count," +
              "                 CASE WHEN dr.rank IS NULL THEN NULL ELSE dr.name END                                             AS donator_role," +
              "                 row_number() OVER (PARTITION BY u.id ORDER BY r.permission::BIGINT DESC, dr.rank ASC NULLS LAST) AS row" +
              "              FROM projects p" +
              "                       JOIN users u ON p.owner_id = u.id" +
              "                       LEFT JOIN user_global_roles gr ON gr.user_id = u.id" +
              "                       LEFT JOIN roles r ON gr.role_id = r.id" +
              "                       LEFT JOIN user_global_roles dgr ON dgr.user_id = u.id" +
              "                       LEFT JOIN roles dr ON dgr.role_id = dr.id) sq" +
              "    WHERE sq.row = 1" +
              "    <sort>" +
              "    OFFSET :offset LIMIT :pageSize")
    @UseStringTemplateEngine
    @RegisterBeanMapper(Author.class)
    List<Author> getAuthors(long offset, long pageSize, @Define String sort);

    @SqlQuery("SELECT sq.name, sq.role, sq.join_date, sq.created_at" +
              "  FROM (SELECT u.name                                                  AS name," +
              "               r.name                                                  AS role," +
              "               u.join_date," +
              "               u.created_at," +
              "               r.permission," +
              "               rank() OVER (PARTITION BY u.name ORDER BY r.permission::BIGINT DESC) AS rank" +
              "          FROM users u" +
              "                 JOIN user_global_roles ugr ON u.id = ugr.user_id" +
              "                 JOIN roles r ON ugr.role_id = r.id" +
              "          WHERE r.name IN ('Hangar_Admin', 'Hangar_Mod')) sq" +
              "  WHERE sq.rank = 1" +
              "    <sort>" +
              "    OFFSET :offset LIMIT :pageSize")
    @UseStringTemplateEngine
    @RegisterBeanMapper(Staff.class)
    List<Staff> getStaff(long offset, long pageSize, @Define String sort);

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
