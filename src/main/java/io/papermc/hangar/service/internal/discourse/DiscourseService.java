package io.papermc.hangar.service.internal.discourse;

import org.springframework.stereotype.Service;

import java.util.Objects;

import io.papermc.hangar.config.hangar.DiscourseConfig;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.discourse.DiscourseError;
import io.papermc.hangar.model.internal.discourse.DiscoursePost;
import io.papermc.hangar.model.internal.projects.ExtendedProjectPage;
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.model.internal.projects.HangarProjectPage;
import io.papermc.hangar.model.internal.versions.HangarVersion;
import io.papermc.hangar.service.internal.projects.ProjectPageService;
import io.papermc.hangar.service.internal.projects.ProjectService;

@Service
public class DiscourseService {

    private final DiscourseApi api;
    private final DiscourseConfig config;
    private final ProjectPageService pageService;
    private final ProjectService projectService;

    public DiscourseService(DiscourseApi api, DiscourseConfig config, ProjectPageService pageService, ProjectService projectService) {
        this.api = api;
        this.config = config;
        this.pageService = pageService;
        this.projectService = projectService;
    }

    // TODO instead of calling api directly, these need to be jobs

    private String getHomepageContent(HangarProject project) {
        long id = -1;
        for (HangarProjectPage page : project.getPages()) {
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

    public void createProjectTopic(HangarProject project) {
        String title = DiscourseFormatter.formatProjectTitle(project);
        String content = DiscourseFormatter.formatProjectTopic(project, getHomepageContent(project));

        DiscoursePost post = api.createTopic(project.getOwner().getName(), title, content, config.getCategory());
        if (post == null) {
            throw new DiscourseError("project post wasn't created");
        }
        if (!post.isTopic()) {
            throw new DiscourseError("project post isn't a topic?!");
        }
        if (!post.getUsername().equals(project.getOwner().getName())) {
            throw new DiscourseError("project post user isn't owner?!");
        }

        projectService.saveDiscourseData(project.getProjectId(), post.getTopicId(), post.getId());
    }

    public void updateProjectTopic(HangarProject project) {
        String title = DiscourseFormatter.formatProjectTitle(project);
        String content = DiscourseFormatter.formatProjectTopic(project, getHomepageContent(project));

        api.updateTopic(project.getOwner().getName(), project.getTopicId(), title, project.getVisibility() == Visibility.PUBLIC ? config.getCategory() : config.getCategoryDeleted());
        api.updatePost(project.getOwner().getName(), project.getPostId(), content);
    }

    public DiscoursePost postDiscussionReply(long topicId, String poster, String content) {
        return api.createPost(poster, topicId, content);
    }

    public void createComment(long projectId, String poster, String content) {
        Objects.requireNonNull(content, "No content");
        ProjectTable projectTable = projectService.getProjectTable(projectId);
        if (projectTable == null) {
            throw new DiscourseError("No project to post to");
        }
        if (projectTable.getTopicId() == null) {
            throw new DiscourseError("No topic to post to");
        }
        postDiscussionReply(projectTable.getTopicId(), poster, content);
    }

    public void createVersionPost(HangarProject project, HangarVersion version) {
        String content = DiscourseFormatter.formatVersionRelease(project, version, version.getDescription());

        DiscoursePost post = postDiscussionReply(project.getTopicId(), project.getOwner().getName(), content);
        projectService.saveDiscourseData(project.getProjectId(), project.getTopicId(), post.getId());
    }

    public void updateVersionPost(HangarProject project, HangarVersion version) {
        Objects.requireNonNull(project.getTopicId(), "No topic ID set");
        Objects.requireNonNull(version.getPostId(), "No post ID set");

        String content = DiscourseFormatter.formatVersionRelease(project, version, version.getDescription());

        api.updatePost(project.getOwner().getName(), version.getPostId().intValue(), content);
    }

    public void deleteTopic(int topicId) {
        api.deleteTopic(config.getAdminUser(), topicId);
    }
}
