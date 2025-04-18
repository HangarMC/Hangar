package io.papermc.hangar.components.jobs;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.jobs.model.JobType;
import io.papermc.hangar.components.jobs.model.ScheduledTaskJob;
import io.papermc.hangar.components.scheduler.SchedulerService;
import io.papermc.hangar.components.webhook.service.WebhookService;
import io.papermc.hangar.components.jobs.db.JobsDAO;
import io.papermc.hangar.components.jobs.db.JobTable;
import io.papermc.hangar.components.jobs.model.Job;
import io.papermc.hangar.components.jobs.model.JobException;
import io.papermc.hangar.components.jobs.model.SendMailJob;
import io.papermc.hangar.components.jobs.model.SendWebhookJob;
import io.papermc.hangar.service.internal.MailService;
import io.papermc.hangar.util.ThreadFactory;
import jakarta.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class JobService extends HangarComponent {

    private final JobsDAO jobsDAO;
    private final MailService mailService;
    private final WebhookService webhookService;
    private final SchedulerService schedulerService;

    private ExecutorService executorService;

    @Autowired
    public JobService(final JobsDAO jobsDAO, @Lazy final MailService mailService, @Lazy final WebhookService webhookService, @Lazy final SchedulerService schedulerService) {
        this.jobsDAO = jobsDAO;
        this.mailService = mailService;
        this.webhookService = webhookService;
        this.schedulerService = schedulerService;
    }

    @PostConstruct
    public void initThreadPool() {
        this.executorService = new ThreadPoolExecutor(1, this.config.jobs().maxConcurrentJobs(), 60, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadFactory("JobService", false));
    }

    public void checkAndProcess() {
        final long awaitingJobs = this.jobsDAO.countAwaitingJobs();
        this.logger.trace("Found {} awaiting jobs", awaitingJobs);
        if (awaitingJobs > 0) {
            final long numberToProcess = Math.max(1, Math.min(awaitingJobs, this.config.jobs().maxConcurrentJobs()));
            for (long i = 0; i < numberToProcess; i++) {
                this.executorService.submit(this::process);
            }
        }
    }

    public List<JobTable> getErroredJobs() {
        return this.jobsDAO.getErroredJobs();
    }

    public void schedule(final Job... jobs) {
        for (final Job job : jobs) {
            this.logger.info("Scheduling job: {}", job);
            this.jobsDAO.save(job.toTable());
        }
    }

    public void scheduleIfNotExists(ScheduledTaskJob scheduledTaskJob) {
        if (this.jobsDAO.exists(scheduledTaskJob.getTaskName()) == 0) {
            schedule(scheduledTaskJob);
        }
    }

    private void process() {
        SecurityContextHolder.getContext().setAuthentication(new JobAuthentication());

        final JobTable jobTable = this.jobsDAO.fetchJob();
        if (jobTable == null) {
            return;
        }

        this.logger.debug("Starting job: {} {} {}", jobTable.getId(), jobTable.getJobType(), jobTable.getJobProperties());

        try {
            this.processJob(jobTable);
            if (jobTable.getJobType() != JobType.SCHEDULED_TASK) {
                // scheduled tasks are retried
                this.jobsDAO.finishJob(jobTable.getId());
            }
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

    private void processJob(final JobTable job) throws Exception {
        switch (job.getJobType()) {
            // email
            case SEND_EMAIL -> {
                final SendMailJob sendMailJob = SendMailJob.loadFromTable(job);
                this.mailService.sendMail(sendMailJob.getSubject(), sendMailJob.getRecipient(), sendMailJob.getText());
            }
            // webhook
            case SEND_WEBHOOK -> {
                final SendWebhookJob sendWebhookJob = SendWebhookJob.loadFromTable(job);
                this.webhookService.sendWebhook(sendWebhookJob);
            }
            // scheduled repeating things
            case SCHEDULED_TASK -> {
                final ScheduledTaskJob scheduledTaskJob = ScheduledTaskJob.loadFromTable(job);
                this.schedulerService.runScheduledTask(scheduledTaskJob.getTaskName());
                this.jobsDAO.retryIn(job.getId(), OffsetDateTime.now().plus(scheduledTaskJob.getInterval(), ChronoUnit.MILLIS), null, null);
            }
            default -> throw new JobException("Unknown job type " + job, "unknown_job_type");
        }
    }

    public static class JobAuthentication extends PreAuthenticatedAuthenticationToken {

        public JobAuthentication() {
            super("JobProcessor", "none", List.of());
        }
    }
}
