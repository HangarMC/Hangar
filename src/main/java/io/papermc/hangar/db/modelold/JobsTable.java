package io.papermc.hangar.db.modelold;


import org.jdbi.v3.postgres.HStore;

import java.time.OffsetDateTime;
import java.util.Map;

import io.papermc.hangar.db.customtypes.JobState;

public class JobsTable {

    private long id;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastUpdated;
    private OffsetDateTime retryAt;
    private String lastError;
    private String lastErrorDescriptor;
    private JobState state;
    private String jobType;
    @HStore
    private Map<String, String> jobProperties;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
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


    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    @HStore
    public Map<String, String> getJobProperties() {
        return jobProperties;
    }

    @HStore
    public void setJobProperties(Map<String, String> jobProperties) {
        this.jobProperties = jobProperties;
    }

}
