package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.OrganizationMembersTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterBeanMapper(OrganizationMembersTable.class)
public interface OrganizationMembersDao {

    @SqlUpdate("INSERT INTO organization_members (user_id, organization_id) VALUES (:userId, :organizationId)")
    void insert(@BindBean OrganizationMembersTable organizationMembersTable);
}
