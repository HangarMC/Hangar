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

@Repository
@RegisterBeanMapper(UsersTable.class)
public interface UserDao {

    @SqlUpdate("insert into users (id, created_at, full_name, name, email, tagline, join_date, read_prompts, is_locked, language) values (:id, :now, :fullName, :name, :email, :tagline, :now, :readPrompts, :isLocked, :language)")
    @Timestamped
    @GetGeneratedKeys
    UsersTable insert(@BindBean UsersTable user);

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
}
