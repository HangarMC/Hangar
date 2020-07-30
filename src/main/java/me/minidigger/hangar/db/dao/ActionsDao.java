package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.LoggedActionsOrganizationTable;
import me.minidigger.hangar.db.model.LoggedActionsPageTable;
import me.minidigger.hangar.db.model.LoggedActionsProjectTable;
import me.minidigger.hangar.db.model.LoggedActionsUserTable;
import me.minidigger.hangar.db.model.LoggedActionsVersionTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.springframework.stereotype.Repository;

@RegisterBeanMapper(LoggedActionsOrganizationTable.class)
@RegisterBeanMapper(LoggedActionsPageTable.class)
@RegisterBeanMapper(LoggedActionsVersionTable.class)
@RegisterBeanMapper(LoggedActionsUserTable.class)
@RegisterBeanMapper(LoggedActionsProjectTable.class)
@Repository
public interface ActionsDao {

}
