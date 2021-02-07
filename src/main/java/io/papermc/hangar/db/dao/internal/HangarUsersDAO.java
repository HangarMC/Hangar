package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import org.apache.commons.lang3.tuple.Pair;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
public interface HangarUsersDAO {

    @RegisterConstructorMapper(UserTable.class)
    @RegisterConstructorMapper(value = OrganizationTable.class, prefix = "o_")
    @SqlQuery("SELECT u.*," +
            "   o.id o_id," +
            "   o.created_at o_created_at," +
            "   o.name o_name," +
            "   o.owner_id o_owner_id," +
            "   o.user_id o_user_id" +
            "   FROM users u " +
            "       LEFT JOIN organizations o ON u.id = o.user_id" +
            "   WHERE u.name = :userName")
    Pair<UserTable, OrganizationTable> getUserAndOrg(String userName);
}
