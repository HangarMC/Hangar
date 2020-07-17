package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.OrganizationsTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterBeanMapper(OrganizationsTable.class)
public interface OrganizationDao {

    @SqlUpdate("insert into organizations (created_at, name, owner_id, user_id) values (:now, :name, :ownerId, :userId)")
    @Timestamped
    @GetGeneratedKeys
    OrganizationsTable insert(@BindBean OrganizationsTable organization);
}
