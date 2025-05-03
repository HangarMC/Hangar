package io.papermc.hangar.components.sitemap;

import io.papermc.hangar.components.sitemap.model.SitemapProject;
import io.papermc.hangar.components.sitemap.model.SitemapUser;
import io.papermc.hangar.components.sitemap.model.SitemapVersion;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@JdbiRepository
@RegisterConstructorMapper(SitemapUser.class)
@RegisterConstructorMapper(SitemapProject.class)
@RegisterConstructorMapper(SitemapVersion.class)
public interface SitemapDAO {

    @SqlQuery("SELECT name, id FROM users")
    List<SitemapUser> getUsers();

    @SqlQuery("SELECT name, id FROM users WHERE lower(name) = lower(:username)")
    SitemapUser getUser(String username);

    @SqlQuery("SELECT slug, id FROM projects WHERE owner_id = :id AND visibility = 0")
    List<SitemapProject> getProjects(long id);

    @SqlQuery("""
        SELECT v.id, v.version_string, c.name AS channel
        FROM project_versions v
        JOIN project_channels c ON v.channel_id = c.id
        WHERE v.project_id = :projectId AND visibility = 0
        """)
    List<SitemapVersion> getVersions(long projectId);

    @SqlQuery("""
        SELECT p.slug FROM project_pages p
        WHERE p.project_id = :projectId AND homepage = false
        """)
    List<String> getPages(long projectId);
}
