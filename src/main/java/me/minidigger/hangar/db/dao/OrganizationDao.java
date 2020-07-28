package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.OrganizationsTable;
import me.minidigger.hangar.model.viewhelpers.Organization;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterBeanMapper(OrganizationsTable.class)
public interface OrganizationDao {

    @SqlUpdate("insert into organizations (created_at, name, owner_id, user_id) values (:now, :name, :ownerId, :userId)")
    @Timestamped
    @GetGeneratedKeys
    OrganizationsTable insert(@BindBean OrganizationsTable organization);


    @SqlQuery("SELECT o.id, o.created_at, o.name, o.owner_id, o.user_id FROM organization_members om JOIN organizations o ON om.organization_id = o.id WHERE om.user_id = :id")
    @RegisterBeanMapper(Organization.class)
    List<Organization> getUserOrgs(long id);
}
