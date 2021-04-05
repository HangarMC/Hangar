package io.papermc.hangar.db.dao.internal.table.stats;

import io.papermc.hangar.model.db.stats.ProjectViewIndividualTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.net.InetAddress;
import java.util.Optional;

@Repository
@RegisterConstructorMapper(ProjectViewIndividualTable.class)
public interface ProjectViewsDAO {

    @Timestamped
    @SqlUpdate("INSERT INTO project_views_individual (created_at, project_id, address, cookie, user_id) VALUES (:now, :projectId, :address, :cookie, :userId)")
    void insert(@BindBean ProjectViewIndividualTable projectViewIndividualTable);

    @SqlQuery("SELECT * FROM project_views_individual WHERE address = :address OR (:userId IS NOT NULL AND user_id = :userId) LIMIT 1")
    Optional<ProjectViewIndividualTable> getIndividualView(Long userId, InetAddress address);
}
