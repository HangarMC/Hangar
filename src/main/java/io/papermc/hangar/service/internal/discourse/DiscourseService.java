package io.papermc.hangar.service.internal.discourse;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

import io.papermc.hangar.config.hangar.DiscourseConfig;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.discourse.DiscourseError;
import io.papermc.hangar.model.internal.discourse.DiscoursePost;
import io.papermc.hangar.model.internal.job.JobException;
import io.papermc.hangar.model.internal.projects.ExtendedProjectPage;
import io.papermc.hangar.model.internal.projects.HangarProjectPage;
import io.papermc.hangar.service.internal.projects.ProjectPageService;
import io.papermc.hangar.service.internal.projects.ProjectService;

/**
 * Never call these methods in here directly, they need to be called as part of a job, with retry and shit
 */
@Service
public class DiscourseService {

    private final DiscourseApi api;
    private final DiscourseConfig config;
    private final ProjectPageService pageService;
    private final ProjectService projectService;
    private final ProjectPageService projectPageService;

    public DiscourseService(DiscourseApi api, DiscourseConfig config, ProjectPageService pageService, ProjectService projectService, ProjectPageService projectPageService) {
        this.api = api;
        this.config = config;
        this.pageService = pageService;
        this.projectService = projectService;
        this.projectPageService = projectPageService;
    }

    private String getHomepageContent(ProjectTable project) {
        long id = -1;
        Map<Long, HangarProjectPage> projectPages = projectPageService.getProjectPages(project.getId());
        for (HangarProjectPage page : projectPages.values()) {
            if (page.isHome()) {
                id = page.getId();
                break;
            }
        }

        if (id == -1) {
            throw new HangarApiException("No homepage found, can't create forum post!");
        }

        ExtendedProjectPage projectPage = pageService.getProjectPage(id);
        if (projectPage == null) {
            throw new HangarApiException("No homepage content found, can't create forum post!");
        }
        return projectPage.getContents();
    }

    public void createProjectTopic(ProjectTable project) {
        String title = DiscourseFormatter.formatProjectTitle(project);
        String content = DiscourseFormatter.formatProjectTopic(project, getHomepageContent(project));

        DiscoursePost post = api.createTopic(project.getOwnerName(), title, content, config.getCategory());
        if (post == null) {
            throw new JobException("project post wasn't created " + project.getProjectId(), "sanity_check");
        }
        if (!post.isTopic()) {
            throw new JobException("project post isn't a topic?! " +  project.getProjectId(), "sanity_check");
        }
        if (!post.getUsername().equals(project.getOwnerName())) {
            throw new JobException("project post user isn't owner?! " +  project.getProjectId(), "sanity_check");
        }

        projectService.saveDiscourseData(project.getProjectId(), post.getTopicId(), post.getId());
    }

    public void updateProjectTopic(ProjectTable project) {
        String title = DiscourseFormatter.formatProjectTitle(project);
        String content = DiscourseFormatter.formatProjectTopic(project, getHomepageContent(project));

        api.updateTopic(project.getOwnerName(), project.getTopicId(), title, project.getVisibility() == Visibility.PUBLIC ? config.getCategory() : config.getCategoryDeleted());
        api.updatePost(project.getOwnerName(), project.getPostId(), content);
    }

    public DiscoursePost postDiscussionReply(long topicId, String poster, String content) {
        return api.createPost(poster, topicId, content);
    }

    public void createComment(long projectId, String poster, String content) {
        Objects.requireNonNull(content, "No content");
        ProjectTable projectTable = projectService.getProjectTable(projectId);
        if (projectTable == null) {
            throw new JobException("No project to post to", "project_not_found");
        }
        if (projectTable.getTopicId() == null) {
            throw new JobException("No topic to post to", "topic_not_found");
        }
        postDiscussionReply(projectTable.getTopicId(), poster, content);
    }

    public void createVersionPost(ProjectTable project, ProjectVersionTable version) {
        String content = DiscourseFormatter.formatVersionRelease(project, version, version.getDescription());

        DiscoursePost post = postDiscussionReply(project.getTopicId(), project.getOwnerName(), content);
        projectService.saveDiscourseData(project.getProjectId(), project.getTopicId(), post.getId());
    }

    public void updateVersionPost(ProjectTable project, ProjectVersionTable version) {
        Objects.requireNonNull(project.getTopicId(), "No topic ID set");
        Objects.requireNonNull(version.getPostId(), "No post ID set");

        String content = DiscourseFormatter.formatVersionRelease(project, version, version.getDescription());

        api.updatePost(project.getOwnerName(), version.getPostId().intValue(), content);
    }

    public void deleteTopic(long topicId) {
        api.deleteTopic(config.getAdminUser(), topicId);
    }
}
