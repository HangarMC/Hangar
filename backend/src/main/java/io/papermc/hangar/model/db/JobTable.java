package io.papermc.hangar.model.db;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.customtypes.JobState;
import io.papermc.hangar.model.internal.job.JobType;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class JobTable extends Table {

    private final OffsetDateTime lastUpdated;
    private final OffsetDateTime retryAt;
    private final String lastError;
    private final String lastErrorDescriptor;
    private final JobState state;
    private final JobType jobType;
    private final JSONB jobProperties;

    public JobTable(final OffsetDateTime lastUpdated, final OffsetDateTime retryAt, final String lastError, final String lastErrorDescriptor, final JobState state, @EnumByName final JobType jobType, final JSONB jobProperties) {
        this.lastUpdated = lastUpdated;
        this.retryAt = retryAt;
        this.lastError = lastError;
        this.lastErrorDescriptor = lastErrorDescriptor;
        this.state = state;
        this.jobType = jobType;
        this.jobProperties = jobProperties;
    }

    @JdbiConstructor
    public JobTable(final OffsetDateTime createdAt, final long id, final OffsetDateTime lastUpdated, final OffsetDateTime retryAt, final String lastError, final String lastErrorDescriptor, final JobState state, @EnumByName final JobType jobType, final JSONB jobProperties) {
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
        return this.lastUpdated;
    }

    public OffsetDateTime getRetryAt() {
        return this.retryAt;
    }

    public String getLastError() {
        return this.lastError;
    }

    public String getLastErrorDescriptor() {
        return this.lastErrorDescriptor;
    }

    public JobState getState() {
        return this.state;
    }

    public JobType getJobType() {
        return this.jobType;
    }

    public JSONB getJobProperties() {
        return this.jobProperties;
    }

    @Override
    public String toString() {
        return "JobTable{" +
            "lastUpdated=" + this.lastUpdated +
            ", retryAt=" + this.retryAt +
            ", lastError='" + this.lastError + '\'' +
            ", lastErrorDescriptor='" + this.lastErrorDescriptor + '\'' +
            ", jobState=" + this.state +
            ", jobType=" + this.jobType +
            ", jobProperties=" + this.jobProperties +
            "} " + super.toString();
    }
}
