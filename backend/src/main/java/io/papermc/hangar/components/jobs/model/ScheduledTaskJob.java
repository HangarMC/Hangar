package io.papermc.hangar.components.jobs.model;

import io.papermc.hangar.components.jobs.db.JobTable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ScheduledTaskJob extends Job {

    private String taskName;
    private long interval;

    public ScheduledTaskJob(final String taskName, final long interval) {
        super(JobType.SCHEDULED_TASK);
        this.taskName = taskName;
        this.interval = interval;
    }

    ScheduledTaskJob() {
        super(JobType.SCHEDULED_TASK);
    }

    public String getTaskName() {
        return this.taskName;
    }

    public long getInterval() {
        return interval;
    }

    @Override
    public void loadFromProperties() {
        if (this.getJobProperties() == null) {
            return;
        }
        if (this.getJobProperties().containsKey("taskName")) {
            this.taskName = this.getJobProperties().get("taskName");
        }
        if (this.getJobProperties().containsKey("interval")) {
            this.interval = Long.parseLong(this.getJobProperties().get("interval"));
        }
    }

    @Override
    public void saveIntoProperties() {
        final Map<String, String> properties = new HashMap<>();
        properties.put("taskName", this.taskName);
        properties.put("interval", String.valueOf(this.interval));
        this.setJobProperties(properties);
    }

    public static ScheduledTaskJob loadFromTable(final JobTable table) {
        final ScheduledTaskJob job = new ScheduledTaskJob();
        job.fromTable(table);
        job.setJobProperties(table.getJobProperties().getMap());
        job.loadFromProperties();
        return job;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final ScheduledTaskJob that = (ScheduledTaskJob) o;
        return Objects.equals(this.taskName, that.taskName) && this.interval == that.interval;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.taskName, this.interval);
    }

    @Override
    public String toString() {
        return "ScheduledTaskJob{" +
            "taskName='" + this.taskName + '\'' +
            ", interval=" + this.interval +
            "} " + super.toString();
    }
}
