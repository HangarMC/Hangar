package io.papermc.hangar.model.internal.job;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.customtypes.JobState;
import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.db.JobTable;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public abstract class Job extends Model {

    private @Nullable OffsetDateTime lastUpdated;
    private @Nullable OffsetDateTime retryAt;
    private @Nullable String lastError;
    private @Nullable String lastErrorDescriptor;

    private JobState state;
    private JobType jobType;

    private Map<String, String> jobProperties;

    Job(final JobType type) {
        this(null, null, null, null, null, JobState.NOT_STARTED, type, new HashMap<>());
    }

    protected Job(final OffsetDateTime createdAt, final @Nullable OffsetDateTime lastUpdated, final @Nullable OffsetDateTime retryAt, final @Nullable String lastError, final @Nullable String lastErrorDescriptor, final JobState state, final JobType jobType, final Map<String, String> jobProperties) {
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
        return this.lastUpdated;
    }

    public void setLastUpdated(final OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public OffsetDateTime getRetryAt() {
        return this.retryAt;
    }

    public void setRetryAt(final OffsetDateTime retryAt) {
        this.retryAt = retryAt;
    }

    public String getLastError() {
        return this.lastError;
    }

    public void setLastError(final String lastError) {
        this.lastError = lastError;
    }

    public String getLastErrorDescriptor() {
        return this.lastErrorDescriptor;
    }

    public void setLastErrorDescriptor(final String lastErrorDescriptor) {
        this.lastErrorDescriptor = lastErrorDescriptor;
    }

    public JobState getState() {
        return this.state;
    }

    public void setState(final JobState state) {
        this.state = state;
    }

    public JobType getJobType() {
        return this.jobType;
    }

    public void setJobType(final JobType jobType) {
        this.jobType = jobType;
    }

    public Map<String, String> getJobProperties() {
        return this.jobProperties;
    }

    public void setJobProperties(final Map<String, String> jobProperties) {
        this.jobProperties = jobProperties;
    }

    public abstract void loadFromProperties();

    public abstract void saveIntoProperties();

    public void fromTable(final JobTable table) {
        this.createdAt = table.getCreatedAt();
        this.lastUpdated = table.getLastUpdated();
        this.retryAt = table.getRetryAt();
        this.lastError = table.getLastError();
        this.lastErrorDescriptor = table.getLastErrorDescriptor();
        this.state = table.getState();
        this.jobType = table.getJobType();
        this.jobProperties = table.getJobProperties().getMap();
    }

    public JobTable toTable() {
        this.saveIntoProperties();
        return new JobTable(this.createdAt, -1, this.lastUpdated, this.retryAt, this.lastError, this.lastErrorDescriptor, this.state, this.jobType, new JSONB(this.jobProperties));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final Job job = (Job) o;
        return Objects.equals(this.lastUpdated, job.lastUpdated) && Objects.equals(this.retryAt, job.retryAt) && Objects.equals(this.lastError, job.lastError) && Objects.equals(this.lastErrorDescriptor, job.lastErrorDescriptor) && Objects.equals(this.state, job.state) && this.jobType == job.jobType && Objects.equals(this.jobProperties, job.jobProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.lastUpdated, this.retryAt, this.lastError, this.lastErrorDescriptor, this.state, this.jobType, this.jobProperties);
    }

    @Override
    public String toString() {
        return "Job{" +
            "createdAt=" + this.createdAt +
            ", lastUpdated=" + this.lastUpdated +
            ", retryAt=" + this.retryAt +
            ", lastError='" + this.lastError + '\'' +
            ", lastErrorDescriptor='" + this.lastErrorDescriptor + '\'' +
            ", state=" + this.state +
            ", jobType=" + this.jobType +
            ", jobProperties=" + this.jobProperties +
            "} " + super.toString();
    }
}
