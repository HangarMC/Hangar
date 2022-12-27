package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.db.extras.BindPagination;
import io.papermc.hangar.db.mappers.LogActionColumnMapper;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.db.log.LoggedActionsOrganizationTable;
import io.papermc.hangar.model.db.log.LoggedActionsPageTable;
import io.papermc.hangar.model.db.log.LoggedActionsProjectTable;
import io.papermc.hangar.model.db.log.LoggedActionsUserTable;
import io.papermc.hangar.model.db.log.LoggedActionsVersionTable;
import io.papermc.hangar.model.internal.logs.HangarLoggedAction;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.DefineNamedBindings;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

@Repository
public interface LoggedActionsDAO {

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

    @UseStringTemplateEngine
    @RegisterColumnMapper(LogActionColumnMapper.class)
    @RegisterConstructorMapper(HangarLoggedAction.class)
    @SqlQuery("SELECT * FROM v_logged_actions la " +
        " WHERE TRUE <filters>" +
        " ORDER BY la.created_at DESC <offsetLimit>")
    // TODO add <sorters>
    @DefineNamedBindings
    List<HangarLoggedAction> getLog(@BindPagination RequestPagination pagination);

    @UseStringTemplateEngine
    @SqlQuery("SELECT count(*) FROM v_logged_actions la " +
        " WHERE TRUE <filters>")
    long getLogCount(@BindPagination(isCount = true) RequestPagination pagination);
}
