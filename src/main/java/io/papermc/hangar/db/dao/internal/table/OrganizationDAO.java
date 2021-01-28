package io.papermc.hangar.db.dao.internal.table;

import io.papermc.hangar.model.db.OrganizationTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(OrganizationTable.class)
public interface OrganizationDAO {

    @SqlUpdate("insert into organizations (id, created_at, name, owner_id, user_id) values (:id, :now, :name, :ownerId, :userId)")
    @Timestamped
    @GetGeneratedKeys
    OrganizationTable insert(@BindFields OrganizationTable organization);

    @SqlQuery("SELECT * FROM organizations WHERE id = :orgId")
    OrganizationTable getById(long orgId);

    @SqlQuery("SELECT * FROM organizations WHERE user_id = :userId")
    OrganizationTable getByUserId(long userId);

    @SqlQuery("SELECT * FROM organizations WHERE name = :username")
    OrganizationTable getByUserName(String username);
}
