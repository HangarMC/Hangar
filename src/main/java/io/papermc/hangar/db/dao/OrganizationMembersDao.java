package io.papermc.hangar.db.dao;

import io.papermc.hangar.db.modelold.OrganizationMembersTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterBeanMapper(OrganizationMembersTable.class)
public interface OrganizationMembersDao {

    @SqlUpdate("INSERT INTO organization_members (user_id, organization_id) VALUES (:userId, :organizationId)")
    void insert(@BindBean OrganizationMembersTable organizationMembersTable);

    @SqlUpdate("DELETE FROM organization_members WHERE organization_id = :orgId AND user_id = :userId")
    int delete(long orgId, long userId);
}
