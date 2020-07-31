package me.minidigger.hangar.db.dao;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;

import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.viewhelpers.Author;
import me.minidigger.hangar.model.viewhelpers.Staff;

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
}
