package io.papermc.hangar.db.dao.internal.table.members;

import io.papermc.hangar.model.db.members.OrganizationMemberTable;
import java.util.Map;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(OrganizationMemberTable.class)
public interface OrganizationMembersDAO extends MembersDAO<OrganizationMemberTable> {

    @Override
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO organization_members (user_id, organization_id) VALUES (:userId, :organizationId)")
    OrganizationMemberTable insert(@BindBean OrganizationMemberTable table);

    @Override
    @SqlQuery("SELECT * FROM organization_members WHERE organization_id = :organizationId AND user_id = :userId")
    OrganizationMemberTable getMemberTable(long organizationId, long userId);

    @Override
    @SqlUpdate("DELETE FROM organization_members WHERE user_id = :userId AND organization_id = :organizationId")
    void delete(long organizationId, long userId);

    @SqlUpdate("UPDATE organization_members SET hidden = :hidden WHERE user_id = :userId AND organization_id = :organizationId")
    void setMembershipVisibility(long organizationId, long userId, boolean hidden);

    @KeyColumn("name")
    @ValueColumn("hidden")
    @SqlQuery("SELECT o.name, uom.hidden" +
        "   FROM organization_members uom" +
        "       JOIN organizations o ON o.id = uom.organization_id" +
        "       JOIN users u ON uom.user_id = u.id" +
        "   WHERE lower(u.name) = lower(:user)")
    Map<String, Boolean> getUserOrganizationMembershipVisibility(String user);
}
