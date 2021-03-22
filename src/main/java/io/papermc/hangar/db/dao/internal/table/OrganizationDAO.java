package io.papermc.hangar.db.dao.internal.table;

import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.OrganizationTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterConstructorMapper(OrganizationTable.class)
public interface OrganizationDAO {

    @SqlUpdate("insert into organizations (id, created_at, name, owner_id, user_id) values (:id, :now, :name, :ownerId, :userId)")
    @Timestamped
    @GetGeneratedKeys
    OrganizationTable insert(@BindBean OrganizationTable organization);

    @SqlQuery("SELECT * FROM organizations WHERE id = :orgId")
    OrganizationTable getById(long orgId);

    @SqlQuery("SELECT * FROM organizations WHERE user_id = :userId")
    OrganizationTable getByUserId(long userId);

    @SqlQuery("SELECT * FROM organizations WHERE name = :name")
    OrganizationTable getByName(String name);

    @SqlQuery("SELECT o.*" +
            "   FROM organization_trust ot" +
            "       JOIN organizations o ON ot.organization_id = o.id" +
            "   WHERE ot.user_id = :userId" +
            "       AND (ot.permission & :permission::bit(64)) = :permission::bit(64)")
    List<OrganizationTable> getOrganizationsWithPermission(long userId, Permission permission);

    @SqlQuery("SELECT * FROM organizations WHERE owner_id = :ownerId")
    List<OrganizationTable> getOrganizationsOwnedBy(long ownerId);
}
