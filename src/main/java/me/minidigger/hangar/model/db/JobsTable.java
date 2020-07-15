package me.minidigger.hangar.model.db;


import java.time.LocalDateTime;

import me.minidigger.hangar.model.db.customtypes.HStore;
import me.minidigger.hangar.model.db.customtypes.JobState;

public class JobsTable {

    private long id;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private LocalDateTime retryAt;
    private String lastError;
    private String lastErrorDescriptor;
    private JobState state;
    private String jobType;
    private HStore jobProperties;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }


    public LocalDateTime getRetryAt() {
        return retryAt;
    }

    public void setRetryAt(LocalDateTime retryAt) {
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


    public HStore getJobProperties() {
        return jobProperties;
    }

    public void setJobProperties(HStore jobProperties) {
        this.jobProperties = jobProperties;
    }

}
