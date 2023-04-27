package io.papermc.hangar.service.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.JobsDAO;
import io.papermc.hangar.model.db.JobTable;
import io.papermc.hangar.model.internal.job.Job;
import io.papermc.hangar.model.internal.job.JobException;
import io.papermc.hangar.model.internal.job.SendMailJob;
import io.papermc.hangar.util.ThreadFactory;
import jakarta.annotation.PostConstruct;
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
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService extends HangarComponent {

    private final JobsDAO jobsDAO;
    private final MailService mailService;

    private ExecutorService executorService;

    @Autowired
    public JobService(final JobsDAO jobsDAO, @Lazy final MailService mailService) {
        this.jobsDAO = jobsDAO;
        this.mailService = mailService;
    }

    @PostConstruct
    public void initThreadPool() {
        this.executorService = new ThreadPoolExecutor(1, this.config.jobs.maxConcurrentJobs(), 60, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadFactory("JobService", false));
    }

    public void checkAndProcess() {
        final long awaitingJobs = this.jobsDAO.countAwaitingJobs();
        this.logger.trace("Found {} awaiting jobs", awaitingJobs);
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
    public void schedule(final Job job) {
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

    public void processJob(final JobTable job) throws Exception {
        switch (job.getJobType()) {
            // email
            case SEND_EMAIL -> {
                final SendMailJob sendMailJob = SendMailJob.loadFromTable(job);
                this.mailService.sendMail(sendMailJob.getSubject(), sendMailJob.getRecipient(), sendMailJob.getText());
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
