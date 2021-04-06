package io.papermc.hangar.db.daoold;

import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationMembersDao {

    @SqlUpdate("DELETE FROM organization_members WHERE organization_id = :orgId AND user_id = :userId")
    int delete(long orgId, long userId);
}
