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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class JobService extends HangarComponent {

    private final JobsDAO jobsDAO;
    private final DiscourseService discourseService;
    private final ProjectService projectService;
    private final VersionService versionService;

    private ExecutorService executorService;

    @Autowired
    public JobService(JobsDAO jobsDAO, @Lazy DiscourseService discourseService, @Lazy ProjectService projectService, @Lazy VersionService versionService) {
        this.jobsDAO = jobsDAO;
        this.discourseService = discourseService;
        this.projectService = projectService;
        this.versionService = versionService;
    }

    @PostConstruct
    public void initThreadPool() {
        this.executorService = new ThreadPoolExecutor(1, config.jobs.getMaxConcurrentJobs(), 60, TimeUnit.SECONDS, new SynchronousQueue<>());
    }

    public void checkAndProcess() {
        if (!config.discourse.isEnabled()) { return; }
        long awaitingJobs = jobsDAO.countAwaitingJobs();
        logger.debug("Found {} awaiting jobs", awaitingJobs);
        if (awaitingJobs > 0) {
            long numberToProcess = Math.max(1, Math.min(awaitingJobs, config.jobs.getMaxConcurrentJobs()));
            for (long i = 0; i < numberToProcess; i++) {
                executorService.submit(this::process);
            }
        }
    }

    public List<JobTable> getErroredJobs() {
        return jobsDAO.getErroredJobs();
    }

    @Transactional
    public void save(Job job) {
        if (!config.discourse.isEnabled()) { return; }
        jobsDAO.save(job.toTable());
    }

    public void process() {
        SecurityContextHolder.getContext().setAuthentication(new JobAuthentication());

        JobTable jobTable = jobsDAO.fetchJob();
        if (jobTable == null) {
            return;
        }

        logger.debug("Starting job: {} {} {}", jobTable.getId(), jobTable.getJobType(), jobTable.getJobProperties());

        try {
            processJob(jobTable);

            jobsDAO.finishJob(jobTable.getId());
        } catch (DiscourseError.RateLimitError rateLimitError) {
            jobsDAO.retryIn(jobTable.getId(), OffsetDateTime.now().plus(rateLimitError.getDuration()).plusSeconds(5), "Rate limit hit", "rate_limit");
        } catch (DiscourseError.StatusError statusError) {
            String error = "Encountered status error when executing Discourse request\n" +
                           toJobString(jobTable) +
                           "Status Code: " + statusError.getStatus() + "\n" +
                           toMessageString(statusError);
            jobsDAO.retryIn(jobTable.getId(), OffsetDateTime.now().plus(config.jobs.getStatusErrorTimeout()).plusSeconds(5), error, "status_error_" + statusError.getStatus().value());
        } catch (DiscourseError.UnknownError unknownError) {
            String error = "Encountered error when executing Discourse request\n" +
                            toJobString(jobTable) +
                           "Type: " + unknownError.getDescriptor() + "\n" +
                           toMessageString(unknownError);
            jobsDAO.retryIn(jobTable.getId(), OffsetDateTime.now().plus(config.jobs.getUnknownErrorTimeout()).plusSeconds(5), error, "unknown_error" + unknownError.getDescriptor());
        } catch (DiscourseError.NotAvailableError notAvailableError) {
            jobsDAO.retryIn(jobTable.getId(), OffsetDateTime.now().plus(config.jobs.getNotAvailableTimeout()).plusSeconds(5), "Not Available", "not_available");
        } catch (DiscourseError.NotProcessable notProcessable) {
            logger.debug("job failed to process discourse job: {} {}", notProcessable.getMessage(), jobTable);
            String error = "Encountered error when processing discourse job\n" +
                            toJobString(jobTable) +
                           "Type: not_processable\n" +
                           toMessageString(notProcessable);
            jobsDAO.fail(jobTable.getId(), error, "not_processable");
        } catch (JobException jobException) {
            logger.debug("job failed to process: {} {}", jobException.getMessage(), jobTable);
            String error = "Encountered error when processing job\n" +
                            toJobString(jobTable) +
                           "Type: " + jobException.getDescriptor() + "\n" +
                           toMessageString(jobException);
            jobsDAO.fail(jobTable.getId(), error, jobException.getDescriptor());
        } catch (Exception ex) {
            logger.debug("job failed to process: {} {}", ex.getMessage(), jobTable, ex);
            String error = "Encountered error when processing job\n" +
                            toJobString(jobTable) +
                           "Exception: " + ex.getClass().getName() + "\n" +
                           toMessageString(ex);
            jobsDAO.fail(jobTable.getId(), error, "exception");
        }
    }

    private String toJobString(JobTable jobTable) {
        return "Job: " + jobTable.getId() + " " + jobTable.getJobType() + " " + jobTable.getJobProperties() + "\n";
    }

    private String toMessageString(Throwable error) {
        return "Message: " + error.getMessage();
    }

    public void processJob(JobTable job) {
        switch (job.getJobType()) {
            case UPDATE_DISCOURSE_PROJECT_TOPIC:
                UpdateDiscourseProjectTopicJob updateDiscourseProjectTopicJob = UpdateDiscourseProjectTopicJob.loadFromTable(job);
                ProjectTable project = projectService.getProjectTable(updateDiscourseProjectTopicJob.getProjectId());
                if (project == null) {
                    throw new JobException("No such project '" + updateDiscourseProjectTopicJob.getProjectId() + "'?", "project_not_found");
                }
                if (project.getTopicId() == null) {
                    discourseService.createProjectTopic(project);
                } else {
                    discourseService.updateProjectTopic(project);
                }
                break;
            case UPDATE_DISCOURSE_VERSION_POST:
                UpdateDiscourseVersionPostJob updateDiscourseVersionPostJob = UpdateDiscourseVersionPostJob.loadFromTable(job);
                ProjectVersionTable version = versionService.getProjectVersionTable(updateDiscourseVersionPostJob.getVersionId());
                if (version == null) {
                    throw new JobException("No such version '" + updateDiscourseVersionPostJob.getVersionId() + "'?", "version_not_found");
                }
                project = projectService.getProjectTable(version.getProjectId());
                if (project == null) {
                    throw new JobException("No such project '" + version.getProjectId() + "'?", "project_not_found");
                }
                if (project.getTopicId() == null) {
                    discourseService.createProjectTopic(project);
                    throw new DiscourseError.RateLimitError(Duration.ofMinutes(2));
                } else if (version.getPostId() == null) {
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
                discourseService.createComment(postDiscourseReplyJob.getProjectId(), postDiscourseReplyJob.getPoster(), postDiscourseReplyJob.getContent());
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
