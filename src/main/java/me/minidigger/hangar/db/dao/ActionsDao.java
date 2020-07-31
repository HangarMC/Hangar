package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.LoggedActionsOrganizationTable;
import me.minidigger.hangar.db.model.LoggedActionsPageTable;
import me.minidigger.hangar.db.model.LoggedActionsProjectTable;
import me.minidigger.hangar.db.model.LoggedActionsUserTable;
import me.minidigger.hangar.db.model.LoggedActionsVersionTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@RegisterBeanMapper(LoggedActionsOrganizationTable.class)
@RegisterBeanMapper(LoggedActionsPageTable.class)
@RegisterBeanMapper(LoggedActionsVersionTable.class)
@RegisterBeanMapper(LoggedActionsUserTable.class)
@RegisterBeanMapper(LoggedActionsProjectTable.class)
@Repository
public interface ActionsDao {

    @Timestamped
    @SqlUpdate("INSERT INTO logged_actions_project (created_at, user_id, address, action, project_id, new_state, old_state) VALUES (:now, :userId, :address, :action, :projectId, :newState, :oldState)")
    void insertProjectLog(@BindBean LoggedActionsProjectTable loggedActionsProjectTable);

    @Timestamped
    @SqlUpdate("INSERT INTO logged_actions_page (created_at, user_id, address, action, project_id, page_id, new_state, old_state) VALUES (:now, :userId, :address, :action, :projectId, :pageId, :newState, :oldState)")
    void insertProjectPageLog(@BindBean LoggedActionsPageTable loggedActionsPageTable);

    @Timestamped
    @SqlUpdate("INSERT INTO logged_actions_version (created_at, user_id, address, action, project_id, version_id, new_state, old_state) VALUES (:now, :userId, :address, :action, :projectId, :versionId, :newState, :oldState)")
    void insertVersionLog(@BindBean LoggedActionsVersionTable loggedActionsVersionTable);

    @Timestamped
    @SqlUpdate("INSERT INTO logged_actions_user (created_at, user_id, address, action, subject_id, new_state, old_state) VALUES (:now, :userId, :address, :action, :subjectId, :newState, :oldState)")
    void insertUserLog(@BindBean LoggedActionsUserTable loggedActionsUserTable);

    @Timestamped
    @SqlUpdate("INSERT INTO logged_actions_organization (created_at, user_id, address, action, organization_id, new_state, old_state) VALUES (:now, :userId, :address, :action, :organizationId, :newState, :oldState)")
    void insertOrganizationLog(@BindBean LoggedActionsOrganizationTable loggedActionsOrganizationTable);
}
