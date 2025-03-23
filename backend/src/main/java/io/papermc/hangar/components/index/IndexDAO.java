package io.papermc.hangar.components.index;

import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.version.Version;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@JdbiRepository
@RegisterConstructorMapper(Project.class)
@RegisterConstructorMapper(Version.class)
public interface IndexDAO {

    // TODO dont use view here
    @SqlQuery("""
        SELECT hp.id,
               hp.created_at,
               hp.name,
               hp.owner_name "owner",
               hp.slug,
               hp.views,
               hp.downloads,
               hp.recent_views,
               hp.recent_downloads,
               hp.stars,
               hp.watchers,
               hp.category,
               hp.description,
               coalesce(hp.last_updated, hp.created_at) AS last_updated,
               hp.visibility,
               hp.links,
               hp.tags,
               hp.license_name,
               hp.license_url,
               hp.license_type,
               hp.keywords,
               hp.donation_enabled,
               hp.donation_subject,
               hp.sponsors,
               hp.avatar,
               hp.avatar_fallback
          FROM home_projects hp
        """)
    List<Project> getAllProjects();

    // TODO dont use view here
    @SqlQuery("SELECT * FROM version_view vv")
    List<Version> getAllVersions();
}
