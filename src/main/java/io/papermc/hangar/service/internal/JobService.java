package io.papermc.hangar.service.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.JobsDAO;
import io.papermc.hangar.model.db.JobTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.job.DeleteDiscourseTopicJob;
import io.papermc.hangar.model.internal.job.Job;
import io.papermc.hangar.model.internal.job.JobException;
import io.papermc.hangar.model.internal.job.PostDiscourseReplyJob;
import io.papermc.hangar.model.internal.job.UpdateDiscourseProjectTopicJob;
import io.papermc.hangar.model.internal.job.UpdateDiscourseVersionPostJob;
import io.papermc.hangar.service.internal.discourse.DiscourseService;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.versions.VersionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService extends HangarComponent {

    private final JobsDAO jobsDAO;
    private final DiscourseService discourseService;
    private final ProjectService projectService;
    private final VersionService versionService;

    @Autowired
    public JobService(HangarDao<JobsDAO> jobsDAO, @Lazy DiscourseService discourseService, @Lazy ProjectService projectService, @Lazy VersionService versionService) {
        this.jobsDAO = jobsDAO.get();
        this.discourseService = discourseService;
        this.projectService = projectService;
        this.versionService = versionService;
    }

    public List<JobTable> getErroredJobs() {
        return jobsDAO.getErroredJobs();
    }

    public void save(Job job) {
        jobsDAO.save(job.toTable());
    }

    public void processJob(JobTable job) {
        switch (job.getJobType()) {
            case UPDATE_DISCOURSE_PROJECT_TOPIC:
                UpdateDiscourseProjectTopicJob updateDiscourseProjectTopicJob = UpdateDiscourseProjectTopicJob.loadFromTable(job);
                ProjectTable project = projectService.getProjectTable(updateDiscourseProjectTopicJob.getProjectId());
                if (project == null) {
                    throw new JobException("No such project?");
                }
                if (project.getTopicId() == null) {
                    discourseService.updateProjectTopic(project);
                } else {
                    discourseService.createProjectTopic(project);
                }
                break;
            case UPDATE_DISCOURSE_VERSION_POST:
                UpdateDiscourseVersionPostJob updateDiscourseVersionPostJob = UpdateDiscourseVersionPostJob.loadFromTable(job);
                ProjectVersionTable version = versionService.getProjectVersionTable(updateDiscourseVersionPostJob.getVersionId());
                if (version == null) {
                    throw new JobException("No such version?");
                }
                project = projectService.getProjectTable(version.getProjectId());
                if (project == null) {
                    throw new JobException("No such project?");
                }
                if (project.getTopicId() == null) {
                    discourseService.createProjectTopic(project);
                }
                if (project.getPostId() == null) {
                    discourseService.createVersionPost(project, version);
                } else {
                    discourseService.updateVersionPost(project, version);
                }
                break;
            case DELETE_DISCOURSE_TOPIC:
                DeleteDiscourseTopicJob deleteDiscourseTopicJob = DeleteDiscourseTopicJob.loadFromTable(job);
                discourseService.deleteTopic(deleteDiscourseTopicJob.getTopicId());
                break;
            case POST_DISCOURSE_REPLY:
                PostDiscourseReplyJob postDiscourseReplyJob = PostDiscourseReplyJob.loadFromTable(job);
                discourseService.createComment(postDiscourseReplyJob.getProjectId(), postDiscourseReplyJob.getPoster(), postDiscourseReplyJob.getPoster());
                break;
            default:
                throw new JobException("Unknown job type " + job);
        }
    }
}
