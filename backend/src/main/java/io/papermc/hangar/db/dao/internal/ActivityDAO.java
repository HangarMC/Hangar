package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.model.internal.admin.activity.FlagActivity;
import io.papermc.hangar.model.internal.admin.activity.ReviewActivity;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@JdbiRepository
public interface ActivityDAO {

    @RegisterConstructorMapper(FlagActivity.class)
    @SqlQuery(" SELECT p.owner_name \"owner\"," +
        "          p.slug," +
        "          pf.resolved_at" +
        "   FROM project_flags pf" +
        "       JOIN projects p ON pf.project_id = p.id" +
        "   WHERE pf.user_id = :userId")
    List<FlagActivity> getFlagActivity(long userId);

    @RegisterConstructorMapper(ReviewActivity.class)
    @SqlQuery("""
        SELECT p.owner_name                                                           owner,
               p.slug,
               pvr.ended_at,
               pv.version_string,
               array(SELECT (jsonb_array_elements(pv.platforms) ->> 'platform')::int) platforms
        FROM project_version_reviews pvr
            JOIN project_versions pv ON pvr.version_id = pv.id
            JOIN projects p ON pv.project_id = p.id
        WHERE pvr.user_id = :userId
        """)
    List<ReviewActivity> getReviewActivity(long userId);
}
