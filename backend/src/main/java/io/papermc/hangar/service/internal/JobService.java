package io.papermc.hangar.service.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.JobsDAO;
import io.papermc.hangar.model.db.JobTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.discourse.DiscourseError;
import io.papermc.hangar.model.internal.job.DeleteDiscourseTopicJob;
import io.papermc.hangar.model.internal.job.Job;
import io.papermc.hangar.model.internal.job.JobException;
import io.papermc.hangar.model.internal.job.PostDiscourseReplyJob;
import io.papermc.hangar.model.internal.job.UpdateDiscourseProjectTopicJob;
import io.papermc.hangar.model.internal.job.UpdateDiscourseVersionPostJob;
import io.papermc.hangar.service.internal.discourse.DiscourseService;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.versions.VersionService;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService extends HangarComponent {

    private final JobsDAO jobsDAO;
    private final DiscourseService discourseService;
    private final ProjectService projectService;
    private final VersionService versionService;

    private ExecutorService executorService;

    @Autowired
    public JobService(final JobsDAO jobsDAO, @Lazy final DiscourseService discourseService, @Lazy final ProjectService projectService, @Lazy final VersionService versionService) {
        this.jobsDAO = jobsDAO;
        this.discourseService = discourseService;
        this.projectService = projectService;
        this.versionService = versionService;
    }

    @PostConstruct
    public void initThreadPool() {
        this.executorService = new ThreadPoolExecutor(1, this.config.jobs.maxConcurrentJobs(), 60, TimeUnit.SECONDS, new SynchronousQueue<>());
    }

    public void checkAndProcess() {
        if (!this.config.discourse.enabled()) {
            return;
        }
        final long awaitingJobs = this.jobsDAO.countAwaitingJobs();
        this.logger.debug("Found {} awaiting jobs", awaitingJobs);
        if (awaitingJobs > 0) {
            final long numberToProcess = Math.max(1, Math.min(awaitingJobs, this.config.jobs.maxConcurrentJobs()));
            for (long i = 0; i < numberToProcess; i++) {
                this.executorService.submit(this::process);
            }
        }
    }

    public List<JobTable> getErroredJobs() {
        return this.jobsDAO.getErroredJobs();
    }

    @Transactional
    public void save(final Job job) {
        if (!this.config.discourse.enabled()) {
            return;
        }
        this.jobsDAO.save(job.toTable());
    }

    public void process() {
        SecurityContextHolder.getContext().setAuthentication(new JobAuthentication());

        final JobTable jobTable = this.jobsDAO.fetchJob();
        if (jobTable == null) {
            return;
        }

        this.logger.debug("Starting job: {} {} {}", jobTable.getId(), jobTable.getJobType(), jobTable.getJobProperties());

        try {
            this.processJob(jobTable);

            this.jobsDAO.finishJob(jobTable.getId());
        } catch (final DiscourseError.RateLimitError rateLimitError) {
            this.jobsDAO.retryIn(jobTable.getId(), OffsetDateTime.now().plus(rateLimitError.getDuration()).plusSeconds(5), "Rate limit hit", "rate_limit");
        } catch (final DiscourseError.StatusError statusError) {
            final String error = "Encountered status error when executing Discourse request\n" +
                this.toJobString(jobTable) +
                "Status Code: " + statusError.getStatus() + "\n" +
                this.toMessageString(statusError);
            this.jobsDAO.retryIn(jobTable.getId(), OffsetDateTime.now().plus(this.config.jobs.statusErrorTimeout()).plusSeconds(5), error, "status_error_" + statusError.getStatus().value());
        } catch (final DiscourseError.UnknownError unknownError) {
            final String error = "Encountered error when executing Discourse request\n" +
                this.toJobString(jobTable) +
                "Type: " + unknownError.getDescriptor() + "\n" +
                this.toMessageString(unknownError);
            this.jobsDAO.retryIn(jobTable.getId(), OffsetDateTime.now().plus(this.config.jobs.unknownErrorTimeout()).plusSeconds(5), error, "unknown_error" + unknownError.getDescriptor());
        } catch (final DiscourseError.NotAvailableError notAvailableError) {
            this.jobsDAO.retryIn(jobTable.getId(), OffsetDateTime.now().plus(this.config.jobs.notAvailableTimeout()).plusSeconds(5), "Not Available", "not_available");
        } catch (final DiscourseError.NotProcessable notProcessable) {
            this.logger.debug("job failed to process discourse job: {} {}", notProcessable.getMessage(), jobTable);
            final String error = "Encountered error when processing discourse job\n" +
                this.toJobString(jobTable) +
                "Type: not_processable\n" +
                this.toMessageString(notProcessable);
            this.jobsDAO.fail(jobTable.getId(), error, "not_processable");
        } catch (final JobException jobException) {
            this.logger.debug("job failed to process: {} {}", jobException.getMessage(), jobTable);
            final String error = "Encountered error when processing job\n" +
                this.toJobString(jobTable) +
                "Type: " + jobException.getDescriptor() + "\n" +
                this.toMessageString(jobException);
            this.jobsDAO.fail(jobTable.getId(), error, jobException.getDescriptor());
        } catch (final Exception ex) {
            this.logger.debug("job failed to process: {} {}", ex.getMessage(), jobTable, ex);
            final String error = "Encountered error when processing job\n" +
                this.toJobString(jobTable) +
                "Exception: " + ex.getClass().getName() + "\n" +
                this.toMessageString(ex);
            this.jobsDAO.fail(jobTable.getId(), error, "exception");
        }
    }

    private String toJobString(final JobTable jobTable) {
        return "Job: " + jobTable.getId() + " " + jobTable.getJobType() + " " + jobTable.getJobProperties() + "\n";
    }

    private String toMessageString(final Throwable error) {
        return "Message: " + error.getMessage();
    }

    public void processJob(final JobTable job) {
        switch (job.getJobType()) {
            case UPDATE_DISCOURSE_PROJECT_TOPIC:
                final UpdateDiscourseProjectTopicJob updateDiscourseProjectTopicJob = UpdateDiscourseProjectTopicJob.loadFromTable(job);
                ProjectTable project = this.projectService.getProjectTable(updateDiscourseProjectTopicJob.getProjectId());
                if (project == null) {
                    throw new JobException("No such project '" + updateDiscourseProjectTopicJob.getProjectId() + "'?", "project_not_found");
                }
                if (project.getTopicId() == null) {
                    this.discourseService.createProjectTopic(project);
                } else {
                    this.discourseService.updateProjectTopic(project);
                }
                break;
            case UPDATE_DISCOURSE_VERSION_POST:
                final UpdateDiscourseVersionPostJob updateDiscourseVersionPostJob = UpdateDiscourseVersionPostJob.loadFromTable(job);
                final ProjectVersionTable version = this.versionService.getProjectVersionTable(updateDiscourseVersionPostJob.getVersionId());
                if (version == null) {
                    throw new JobException("No such version '" + updateDiscourseVersionPostJob.getVersionId() + "'?", "version_not_found");
                }
                project = this.projectService.getProjectTable(version.getProjectId());
                if (project == null) {
                    throw new JobException("No such project '" + version.getProjectId() + "'?", "project_not_found");
                }
                if (project.getTopicId() == null) {
                    this.discourseService.createProjectTopic(project);
                    throw new DiscourseError.RateLimitError(Duration.ofMinutes(2));
                } else if (version.getPostId() == null) {
                    this.discourseService.createVersionPost(project, version);
                } else {
                    this.discourseService.updateVersionPost(project, version);
                }
                break;
            case DELETE_DISCOURSE_TOPIC:
                final DeleteDiscourseTopicJob deleteDiscourseTopicJob = DeleteDiscourseTopicJob.loadFromTable(job);
                this.discourseService.deleteTopic(deleteDiscourseTopicJob.getTopicId());
                break;
            case POST_DISCOURSE_REPLY:
                final PostDiscourseReplyJob postDiscourseReplyJob = PostDiscourseReplyJob.loadFromTable(job);
                this.discourseService.createComment(postDiscourseReplyJob.getProjectId(), postDiscourseReplyJob.getPoster(), postDiscourseReplyJob.getContent());
                break;
            default:
                throw new JobException("Unknown job type " + job, "unknown_job_type");
        }
    }

    public static class JobAuthentication extends PreAuthenticatedAuthenticationToken {

        public JobAuthentication() {
            super("JobProcessor", "none", List.of());
        }
    }
}
