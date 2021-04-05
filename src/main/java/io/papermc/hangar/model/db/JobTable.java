package io.papermc.hangar.model.db;

import io.papermc.hangar.db.customtypes.JobState;
import io.papermc.hangar.model.internal.JobType;
import org.jdbi.v3.core.enums.EnumByName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jdbi.v3.postgres.HStore;

import java.time.OffsetDateTime;
import java.util.Map;

public class JobTable extends Table {

    private final OffsetDateTime lastUpdated;
    private final OffsetDateTime retryAt;
    private final String lastError;
    private final String lastErrorDescriptor;
    private final JobState state;
    private final JobType jobType;
    private final Map<String, String> jobProperties;

    public JobTable(OffsetDateTime lastUpdated, OffsetDateTime retryAt, String lastError, String lastErrorDescriptor, JobState state, @EnumByName JobType jobType, Map<String, String> jobProperties) {
        this.lastUpdated = lastUpdated;
        this.retryAt = retryAt;
        this.lastError = lastError;
        this.lastErrorDescriptor = lastErrorDescriptor;
        this.state = state;
        this.jobType = jobType;
        this.jobProperties = jobProperties;
    }

    @JdbiConstructor
    public JobTable(OffsetDateTime createdAt, long id, OffsetDateTime lastUpdated, OffsetDateTime retryAt, String lastError, String lastErrorDescriptor, JobState state, @EnumByName JobType jobType, @HStore Map<String, String> jobProperties) {
        super(createdAt, id);
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

    public OffsetDateTime getRetryAt() {
        return retryAt;
    }

    public String getLastError() {
        return lastError;
    }

    public String getLastErrorDescriptor() {
        return lastErrorDescriptor;
    }

    public JobState getState() {
        return state;
    }

    public JobType getJobType() {
        return jobType;
    }

    public Map<String, String> getJobProperties() {
        return jobProperties;
    }

    @Override
    public String toString() {
        return "JobTable{" +
                "lastUpdated=" + lastUpdated +
                ", retryAt=" + retryAt +
                ", lastError='" + lastError + '\'' +
                ", lastErrorDescriptor='" + lastErrorDescriptor + '\'' +
                ", jobState=" + state +
                ", jobType=" + jobType +
                ", jobProperties=" + jobProperties +
                "} " + super.toString();
    }
}
