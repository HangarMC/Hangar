package me.minidigger.hangar.db.model;


import java.net.InetAddress;
import java.time.OffsetDateTime;

import me.minidigger.hangar.db.customtypes.LoggedAction;

public class LoggedActionsUserTable {

    private long id;
    private OffsetDateTime createdAt;
    private long userId;
    private InetAddress address;
    private LoggedAction action;
    private long subjectId;
    private String newState;
    private String oldState;

    public LoggedActionsUserTable(long userId, InetAddress address, LoggedAction action, long subjectId, String newState, String oldState) {
        this.userId = userId;
        this.address = address;
        this.action = action;
        this.subjectId = subjectId;
        this.newState = newState;
        this.oldState = oldState;
    }

    public LoggedActionsUserTable() { }

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


    public long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(long subjectId) {
        this.subjectId = subjectId;
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
