package io.papermc.hangar.db.dao;

import io.papermc.hangar.db.extras.BindPagination;
import io.papermc.hangar.db.mappers.factories.RoleColumnMapperFactory;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.internal.user.HangarUser;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapperFactory;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.AllowUnusedBindings;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.MapTo;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(HangarUser.class)
@RegisterConstructorMapper(User.class)
@RegisterColumnMapperFactory(RoleColumnMapperFactory.class)
public interface UsersDAO {

    @SqlQuery("""
            SELECT u.id,
                   u.uuid,
                   u.created_at,
                   u.name,
                   u.email,
                   u.tagline,
                   array(SELECT role_id FROM user_global_roles WHERE u.id = user_id) AS roles,
                   (SELECT count(*)
                       FROM project_members_all pma
                       WHERE pma.user_id = u.id
                   ) AS project_count,
                   u.read_prompts,
                   u.locked,
                   u.language,
                   u.theme,
                   u.socials,
                   exists(SELECT 1 FROM organizations o WHERE u.id = o.user_id) AS is_organization
               FROM users u
               WHERE lower(u.name) = lower(:name)
                   OR u.id = :id
               GROUP BY u.id
     """)
    <T extends User> T _getUser(String name, Long id, @MapTo Class<T> type);

    default <T extends User> T getUser(final String name, final Class<T> type) {
        return this._getUser(name, null, type);
    }

    default <T extends User> T getUser(final long id, final Class<T> type) {
        return this._getUser(null, id, type);
    }

    @AllowUnusedBindings // query can be unused
    @UseStringTemplateEngine
    @SqlQuery("SELECT u.id," +
        "       u.created_at," +
        "       u.name," +
        "       u.tagline," +
        "       array(SELECT role_id FROM user_global_roles WHERE u.id = user_id) AS roles," +
        "       (SELECT count(*) FROM project_members_all pma WHERE pma.user_id = u.id) AS project_count," +
        "       u.read_prompts," +
        "       u.locked," +
        "       u.language," +
        "       u.theme," +
        "       exists(SELECT 1 FROM organizations o WHERE u.id = o.user_id) AS is_organization" +
        "   FROM users u" +
        "   <if(hasQuery)>WHERE u.name ILIKE '%' || :query || '%'<endif>" +
        "   GROUP BY u.id " +
        "   <sorters>" +
        "   <offsetLimit>")
    <T extends User> List<T> getUsers(@Define boolean hasQuery, String query, @BindPagination RequestPagination pagination, @MapTo Class<T> type);

    @AllowUnusedBindings // query can be unused
    @UseStringTemplateEngine
    @SqlQuery("SELECT count(*)" +
        "   FROM users u" +
        "   <if(hasQuery)>WHERE u.name ILIKE '%' || :query || '%'<endif>")
    long getUsersCount(@Define boolean hasQuery, String query);
}
