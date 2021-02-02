package io.papermc.hangar.db.dao;

import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.internal.user.HangarUser;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.MapTo;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterConstructorMapper(HangarUser.class)
@RegisterConstructorMapper(User.class)
public interface UsersDAO {

    @SqlQuery("SELECT u.id, " +
            "       u.created_at," +
            "       u.name," +
            "       u.tagline," +
            "       u.join_date, " +
            "       array_agg(r.name) roles," +
            "       (SELECT count(*)" +
            "           FROM project_members_all pma" +
            "           WHERE pma.user_id = u.id" +
            "       ) AS project_count" +
            "   FROM users u" +
            "       JOIN user_global_roles ugr ON u.id = ugr.user_id" +
            "       JOIN roles r ON ugr.role_id = r.id" +
            "   WHERE u.name = :name" +
            "       OR u.id = :id" +
            "   GROUP BY u.id")
    <T extends User> T _getUser(String name, Long id, @MapTo Class<T> type);

    default  <T extends User> T getUser(String name, Class<T> type) {
        return _getUser(name, null, type);
    }

    default <T extends User> T getUser(long id, Class<T> type) {
        return _getUser(null, id, type);
    }

    @SqlQuery("SELECT u.id," +
            "       u.created_at," +
            "       u.name," +
            "       u.tagline," +
            "       u.join_date," +
            "       ARRAY(SELECT r.name FROM roles r JOIN user_global_roles ugr ON r.id = ugr.role_id WHERE u.id = ugr.user_id) roles," +
            "       (SELECT count(*) FROM project_members_all pma WHERE pma.user_id = u.id) AS project_count" +
            "   FROM users u" +
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
