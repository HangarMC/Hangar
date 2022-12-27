package io.papermc.hangar.service.internal.discourse;

import io.papermc.hangar.config.hangar.DiscourseConfig;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.discourse.DiscoursePost;
import io.papermc.hangar.model.internal.job.JobException;
import io.papermc.hangar.model.internal.projects.ExtendedProjectPage;
import io.papermc.hangar.model.internal.projects.HangarProjectPage;
import io.papermc.hangar.service.internal.projects.ProjectPageService;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.versions.VersionService;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Never call these methods in here directly, they need to be called as part of a job, with retry and shit
 */
@Service
public class DiscourseService {

    private final DiscourseApi api;
    private final DiscourseConfig config;
    private final ProjectPageService pageService;
    private final ProjectService projectService;
    private final VersionService versionService;
    private final ProjectPageService projectPageService;
    private final DiscourseFormatter discourseFormatter;

    @Autowired
    public DiscourseService(final DiscourseApi api, final DiscourseConfig config, final ProjectPageService pageService, final ProjectService projectService, final VersionService versionService, final ProjectPageService projectPageService, final DiscourseFormatter discourseFormatter) {
        this.api = api;
        this.config = config;
        this.pageService = pageService;
        this.projectService = projectService;
        this.versionService = versionService;
        this.projectPageService = projectPageService;
        this.discourseFormatter = discourseFormatter;
    }

    private String getHomepageContent(final ProjectTable project) {
        long id = -1;
        final Map<Long, HangarProjectPage> projectPages = this.projectPageService.getProjectPages(project.getId());
        for (final HangarProjectPage page : projectPages.values()) {
            if (page.isHome()) {
                id = page.getId();
                break;
            }
        }

        if (id == -1) {
            throw new HangarApiException("No homepage found, can't create forum post!");
        }

        final ExtendedProjectPage projectPage = this.pageService.getProjectPage(id);
        if (projectPage == null) {
            throw new HangarApiException("No homepage content found, can't create forum post!");
        }
        return projectPage.getContents();
    }

    public void createProjectTopic(final ProjectTable project) {
        final String title = this.discourseFormatter.formatProjectTitle(project);
        final String content = this.discourseFormatter.formatProjectTopic(project, this.getHomepageContent(project));

        final DiscoursePost post = this.api.createTopic(project.getOwnerName(), title, content, this.config.category());
        if (post == null) {
            throw new JobException("project post wasn't created " + project.getProjectId(), "sanity_check");
        }
        if (!post.isTopic()) {
            throw new JobException("project post isn't a topic?! " + project.getProjectId(), "sanity_check");
        }
        if (!post.getUsername().equals(project.getOwnerName())) {
            throw new JobException("project post user isn't owner?! " + project.getProjectId(), "sanity_check");
        }

        this.projectService.saveDiscourseData(project, post.getTopicId(), post.getId());
    }

    public void updateProjectTopic(final ProjectTable project) {
        final String title = this.discourseFormatter.formatProjectTitle(project);
        final String content = this.discourseFormatter.formatProjectTopic(project, this.getHomepageContent(project));

        this.api.updateTopic(project.getOwnerName(), project.getTopicId(), title, project.getVisibility() == Visibility.PUBLIC ? this.config.category() : this.config.categoryDeleted());
        this.api.updatePost(project.getOwnerName(), project.getPostId(), content);
    }

    public DiscoursePost postDiscussionReply(final long topicId, final String poster, final String content) {
        return this.api.createPost(poster, topicId, content);
    }

    public void createComment(final long projectId, final String poster, final String content) {
        Objects.requireNonNull(content, "No content");
        final ProjectTable projectTable = this.projectService.getProjectTable(projectId);
        if (projectTable == null) {
            throw new JobException("No project to post to", "project_not_found");
        }
        if (projectTable.getTopicId() == null) {
            throw new JobException("No topic to post to", "topic_not_found");
        }
        this.postDiscussionReply(projectTable.getTopicId(), poster, content);
    }

    public void createVersionPost(final ProjectTable project, final ProjectVersionTable version) {
        final String content = this.discourseFormatter.formatVersionRelease(project, version, version.getDescription());

        final DiscoursePost post = this.postDiscussionReply(project.getTopicId(), project.getOwnerName(), content);
        this.versionService.saveDiscourseData(version, post.getId());
    }

    public void updateVersionPost(final ProjectTable project, final ProjectVersionTable version) {
        Objects.requireNonNull(project.getTopicId(), "No topic ID set");
        Objects.requireNonNull(version.getPostId(), "No post ID set");

        final String content = this.discourseFormatter.formatVersionRelease(project, version, version.getDescription());

        this.api.updatePost(project.getOwnerName(), version.getPostId().intValue(), content);
    }

    public void deleteTopic(final long topicId) {
        this.api.deleteTopic(this.config.adminUser(), topicId);
    }
}
