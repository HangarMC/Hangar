package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.ProjectsTable;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface ProjectDao {

    @SqlUpdate("insert into projects (id, created_at, plugin_id, name, slug, owner_name, recommended_version_id, owner_id, topic_id, post_id, category, description, visibility, notes, keywords, homepage, issues, source, support, license_name, license_url, forum_sync) values (:id, :now, :pluginId, :name, :slug, :ownerName, :recommendedVersion, :ownerId, :topicId, :postId, :category, :description, :visibility, :notes, :keywords, :homepage, :issues, :source, :support, :licenseName, :licenseUrl, :forumSync)")
    @Timestamped
    @GetGeneratedKeys
    ProjectsTable insert(@BindBean ProjectsTable project);
}
