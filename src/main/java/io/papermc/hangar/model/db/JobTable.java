package io.papermc.hangar.model.db;

import org.jdbi.v3.core.enums.EnumByName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.customtypes.JobState;
import io.papermc.hangar.model.internal.job.JobType;

public class JobTable extends Table {

    private final OffsetDateTime lastUpdated;
    private final OffsetDateTime retryAt;
    private final String lastError;
    private final String lastErrorDescriptor;
    private final JobState state;
    private final JobType jobType;
    private final JSONB jobProperties;

    public JobTable(OffsetDateTime lastUpdated, OffsetDateTime retryAt, String lastError, String lastErrorDescriptor, JobState state, @EnumByName JobType jobType, JSONB jobProperties) {
        this.lastUpdated = lastUpdated;
        this.retryAt = retryAt;
        this.lastError = lastError;
        this.lastErrorDescriptor = lastErrorDescriptor;
        this.state = state;
        this.jobType = jobType;
        this.jobProperties = jobProperties;
    }

    @JdbiConstructor
    public JobTable(OffsetDateTime createdAt, long id, OffsetDateTime lastUpdated, OffsetDateTime retryAt, String lastError, String lastErrorDescriptor, JobState state, @EnumByName JobType jobType, JSONB jobProperties) {
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

    public JSONB getJobProperties() {
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
