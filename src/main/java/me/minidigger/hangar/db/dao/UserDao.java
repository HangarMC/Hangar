package me.minidigger.hangar.db.dao;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import me.minidigger.hangar.db.model.UsersTable;

@Repository
@RegisterBeanMapper(UsersTable.class)
public interface UserDao {

    @SqlUpdate("insert into users (id, full_name, name, email, tagline, join_date, read_prompts, is_locked, language) values (:id, :fullName, :name, :email, :tagline, :now, :readPrompts, :isLocked, :language)")
    @Timestamped
    @GetGeneratedKeys
    UsersTable insert(@BindBean UsersTable user);

    @SqlQuery("select * from users where id = :id")
    UsersTable getById(long id);

    @SqlQuery("select * from users where name = :name")
    UsersTable getByName(String name);
}
