package io.papermc.hangar.model.internal.job;

import io.papermc.hangar.db.customtypes.JobState;
import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.db.JobTable;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class Job extends Model {

    private @Nullable OffsetDateTime lastUpdated;
    private @Nullable OffsetDateTime retryAt;
    private @Nullable String lastError;
    private @Nullable String lastErrorDescriptor;

    private JobState state;
    private JobType jobType;

    private Map<String, String> jobProperties;

    Job(JobType type) {
        this(null, null, null, null, null, JobState.NOT_STARTED, type, new HashMap<>());
    }

    protected Job(OffsetDateTime createdAt, @Nullable OffsetDateTime lastUpdated, @Nullable OffsetDateTime retryAt, @Nullable String lastError, @Nullable String lastErrorDescriptor, JobState state, JobType jobType, Map<String, String> jobProperties) {
        super(createdAt);
        this.lastUpdated = lastUpdated;
        this.retryAt = retryAt;
        this.lastError = lastError;
        this.lastErrorDescriptor = lastErrorDescriptor;
        this.state = state;
        this.jobType = jobType;
        this.jobProperties = jobProperties;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public OffsetDateTime getRetryAt() {
        return retryAt;
    }

    public void setRetryAt(OffsetDateTime retryAt) {
        this.retryAt = retryAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public String getLastErrorDescriptor() {
        return lastErrorDescriptor;
    }

    public void setLastErrorDescriptor(String lastErrorDescriptor) {
        this.lastErrorDescriptor = lastErrorDescriptor;
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public Map<String, String> getJobProperties() {
        return jobProperties;
    }

    public void setJobProperties(Map<String, String> jobProperties) {
        this.jobProperties = jobProperties;
    }

    public abstract void loadFromProperties();
    public abstract void saveIntoProperties();

    public void fromTable(JobTable table) {
        this.createdAt = table.getCreatedAt();
        this.lastUpdated = table.getLastUpdated();
        this.retryAt = table.getRetryAt();
        this.lastError = table.getLastError();
        this.lastErrorDescriptor = table.getLastErrorDescriptor();
        this.state = table.getState();
        this.jobType = table.getJobType();
        this.jobProperties = table.getJobProperties();
    }

    public JobTable toTable() {
        saveIntoProperties();
        return new JobTable(createdAt, -1, lastUpdated, retryAt, lastError, lastErrorDescriptor, state, jobType, jobProperties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Job job = (Job) o;
        return Objects.equals(lastUpdated, job.lastUpdated) && Objects.equals(retryAt, job.retryAt) && Objects.equals(lastError, job.lastError) && Objects.equals(lastErrorDescriptor, job.lastErrorDescriptor) && Objects.equals(state, job.state) && jobType == job.jobType && Objects.equals(jobProperties, job.jobProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lastUpdated, retryAt, lastError, lastErrorDescriptor, state, jobType, jobProperties);
    }

    @Override
    public String toString() {
        return "Job{" +
               "createdAt=" + createdAt +
               ", lastUpdated=" + lastUpdated +
               ", retryAt=" + retryAt +
               ", lastError='" + lastError + '\'' +
               ", lastErrorDescriptor='" + lastErrorDescriptor + '\'' +
               ", state=" + state +
               ", jobType=" + jobType +
               ", jobProperties=" + jobProperties +
               "} " + super.toString();
    }
}
