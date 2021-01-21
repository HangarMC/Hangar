package io.papermc.hangar.db.dao;

import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.internal.HangarUser;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.MapTo;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterConstructorMapper(HangarUser.class)
@RegisterConstructorMapper(User.class)
public interface UsersDAO {

    @SqlQuery("SELECT u.id, u.created_at, u.name, u.tagline, u.join_date, array_agg(r.name) roles " +
            " FROM users u" +
            "     JOIN user_global_roles ugr ON u.id = ugr.user_id" +
            "     JOIN roles r ON ugr.role_id = r.id" +
            " WHERE u.name = :name" +
            " GROUP BY u.id")
    <T extends User> T getUser(String name, @MapTo Class<T> type);

    @SqlQuery("SELECT u.id," +
            "       u.created_at," +
            "       u.name," +
            "       u.tagline," +
            "       u.join_date," +
            "       array_agg(r.name) roles" +
            "   FROM users u" +
            "       JOIN user_global_roles ugr ON u.id = ugr.user_id" +
            "       JOIN roles r ON ugr.role_id = r.id" +
            "   WHERE u.name ILIKE '%' || :query || '%' " +
            "   GROUP BY u.id " +
            "   LIMIT :limit OFFSET :offset")
    <T extends User> List<T> getUsers(String query, long limit, long offset, @MapTo Class<T> type);

    @SqlQuery("SELECT COUNT(*)" +
            "   FROM users u" +
            "       JOIN user_global_roles ugr ON u.id = ugr.user_id" +
            "       JOIN roles r ON ugr.role_id = r.id" +
            "   WHERE u.name ILIKE '%' || :query || '%'")
    long getUsersCount(String query);
}
