package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.db.mappers.LoggedActionViewModelMapper;
import io.papermc.hangar.model.db.log.LoggedActionsOrganizationTable;
import io.papermc.hangar.model.db.log.LoggedActionsPageTable;
import io.papermc.hangar.model.db.log.LoggedActionsProjectTable;
import io.papermc.hangar.model.db.log.LoggedActionsUserTable;
import io.papermc.hangar.model.db.log.LoggedActionsVersionTable;
import io.papermc.hangar.modelold.viewhelpers.LoggedActionViewModel;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.DefineNamedBindings;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    // TODO update
    @UseStringTemplateEngine
    @RegisterRowMapper(LoggedActionViewModelMapper.class)
    @SqlQuery("SELECT * FROM v_logged_actions la " +
              " WHERE true " +
              "<if(userFilter)>AND la.user_name = :userFilter<endif> " +
              "<if(projectFilter)>AND (la.p_owner_name || '/' || la.p_slug) = :projectFilter<endif> " +
              "<if(versionFilter)>AND la.pv_version_string = :versionFilter<endif> " +
              "<if(pageFilter)>AND la.pp_id = :pageFilter<endif> " +
              "<if(actionFilter)>AND la.action = :actionFilter::LOGGED_ACTION_TYPE<endif> " +
              "<if(subjectFilter)>AND la.s_name = :subjectFilter<endif> " +
              "ORDER BY la.created_at DESC OFFSET :offset LIMIT :pageSize")
    @DefineNamedBindings
    List<LoggedActionViewModel<?>> getLog(String userFilter, String projectFilter, String versionFilter, String pageFilter, String actionFilter, String subjectFilter, long offset, long pageSize);
}
