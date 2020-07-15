package me.minidigger.hangar.db.model;


import java.net.InetAddress;
import java.time.OffsetDateTime;

import me.minidigger.hangar.db.customtypes.LoggedAction;

public class LoggedActionsVersionTable {

    private long id;
    private OffsetDateTime createdAt;
    private long userId;
    private InetAddress address;
    private LoggedAction action;
    private long projectId;
    private long versionId;
    private String newState;
    private String oldState;


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


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }


    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }


    public LoggedAction getAction() {
        return action;
    }

    public void setAction(LoggedAction action) {
        this.action = action;
    }


    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }


    public long getVersionId() {
        return versionId;
    }

    public void setVersionId(long versionId) {
        this.versionId = versionId;
    }


    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }


    public String getOldState() {
        return oldState;
    }

    public void setOldState(String oldState) {
        this.oldState = oldState;
    }

}
