package io.papermc.hangar.db.mappers;

import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.AbstractContext;
import io.papermc.hangar.db.customtypes.LoggedActionType.OrganizationContext;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectPageContext;
import io.papermc.hangar.db.customtypes.LoggedActionType.UserContext;
import io.papermc.hangar.db.customtypes.LoggedActionType.VersionContext;
import io.papermc.hangar.modelold.viewhelpers.LoggedActionViewModel;
import io.papermc.hangar.modelold.viewhelpers.LoggedProject;
import io.papermc.hangar.modelold.viewhelpers.LoggedProjectPage;
import io.papermc.hangar.modelold.viewhelpers.LoggedProjectVersion;
import io.papermc.hangar.modelold.viewhelpers.LoggedSubject;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class LoggedActionViewModelMapper implements RowMapper<LoggedActionViewModel<?>> {

    @Override
    public LoggedActionViewModel<?> map(ResultSet rs, StatementContext ctx) throws SQLException {
        long userId = rs.getLong("user_id");
        String userName = rs.getString("user_name");
        String address = rs.getString("address");
        LoggedActionType<? extends AbstractContext<?>> action = LoggedActionType.getLoggedActionType(rs.getString("action"));
        String newState = rs.getString("new_state");
        String oldState = rs.getString("old_state");
        OffsetDateTime createdAt = ctx.findColumnMapperFor(OffsetDateTime.class).get().map(rs, "created_at", ctx);

        int contextType = rs.getInt("context_type");
        final LoggedProject project = new LoggedProject(
                rs.getLong("p_id"),
                rs.getString("p_slug"),
                rs.getString("p_owner_name")
        );

        switch (contextType) {
            case 0:
                return new LoggedActionViewModel<>(userId,
                        userName,
                        address,
                        (LoggedActionType<ProjectContext>) action,
                        ProjectContext.of(rs.getLong("p_id")),
                        newState,
                        oldState,
                        project,
                        null,
                        null,
                        null,
                        createdAt);
            case 1:
                return new LoggedActionViewModel<>(
                        userId,
                        userName,
                        address,
                        (LoggedActionType<VersionContext>) action,
                        VersionContext.of(rs.getLong("p_id"), rs.getLong("pv_id")),
                        newState,
                        oldState,
                        project,
                        new LoggedProjectVersion(
                                rs.getLong("pv_id"),
                                rs.getString("pv_version_string")
                        ),
                        null,
                        null,
                        createdAt
                );
            case 2:
                return new LoggedActionViewModel<>(
                        userId,
                        userName,
                        address,
                        (LoggedActionType<ProjectPageContext>) action,
                        ProjectPageContext.of(rs.getLong("p_id"), rs.getLong("pp_id")),
                        newState,
                        oldState,
                        project,
                        null,
                        new LoggedProjectPage(
                                rs.getLong("pp_id"),
                                rs.getString("pp_name"),
                                rs.getString("pp_slug")
                        ),
                        null,
                        createdAt
                );
            case 3:
                return new LoggedActionViewModel<>(
                        userId,
                        userName,
                        address,
                        (LoggedActionType<UserContext>) action,
                        UserContext.of(rs.getLong("s_id")),
                        newState,
                        oldState,
                        project,
                        null,
                        null,
                        new LoggedSubject(
                                rs.getLong("s_id"),
                                rs.getString("s_name")
                        ),
                        createdAt
                );
            case 4:
                return new LoggedActionViewModel<>(
                        userId,
                        userName,
                        address,
                        (LoggedActionType<OrganizationContext>) action,
                        OrganizationContext.of(rs.getLong("s_id")),
                        newState,
                        oldState,
                        project,
                        null,
                        null,
                        new LoggedSubject(
                                rs.getLong("s_id"),
                                rs.getString("s_name")
                        ),
                        createdAt
                );
            default:
                throw new IllegalArgumentException("Should be a value from 0 - 4");
        }
    }
}
