package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.db.customtypes.LoggedActionType;

import java.time.OffsetDateTime;

public class LoggedActionViewModel {

    private long userId;
    private String userName;
    private String address;
    private LoggedActionType action;
    private LoggedActionType.AbstractContext actionContext;
    private String newState;
    private String oldState;
    private LoggedProject project;
    private LoggedProjectVersion version;
    private LoggedProjectPage page;
    private LoggedSubject subject;
    private OffsetDateTime createdAt;

    public LoggedActionViewModel(long userId, String userName, String address, LoggedActionType action, LoggedActionType.AbstractContext actionContext, String newState, String oldState, LoggedProject project, LoggedProjectVersion version, LoggedProjectPage page, LoggedSubject subject, OffsetDateTime createdAt) {
        this.userId = userId;
        this.userName = userName;
        this.address = address;
        this.action = action;
        this.actionContext = actionContext;
        this.newState = newState;
        this.oldState = oldState;
        this.project = project;
        this.version = version;
        this.page = page;
        this.subject = subject;
        this.createdAt = createdAt;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getAddress() {
        return address;
    }

    public LoggedActionType getAction() {
        return action;
    }

    public LoggedActionType.AbstractContext getActionContext() {
        return actionContext;
    }

    public String getNewState() {
        return newState;
    }

    public String getOldState() {
        return oldState;
    }

    public LoggedProject getProject() {
        return project;
    }

    public LoggedProjectVersion getVersion() {
        return version;
    }

    public LoggedProjectPage getPage() {
        return page;
    }

    public LoggedSubject getSubject() {
        return subject;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
