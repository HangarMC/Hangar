package io.papermc.hangar.modelold.viewhelpers;

import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.AbstractContext;

import java.time.OffsetDateTime;

public class LoggedActionViewModel<C extends AbstractContext<C>> {

    private final long userId;
    private final String userName;
    private final String address;
    private final LoggedActionType<C> action;
    private final C actionContext;
    private final String newState;
    private final String oldState;
    private final LoggedProject project;
    private final LoggedProjectVersion version;
    private final LoggedProjectPage page;
    private final LoggedSubject subject;
    private final OffsetDateTime createdAt;

    public LoggedActionViewModel(long userId, String userName, String address, LoggedActionType<C> action, C actionContext, String newState, String oldState, LoggedProject project, LoggedProjectVersion version, LoggedProjectPage page, LoggedSubject subject, OffsetDateTime createdAt) {
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

    public LoggedActionType<C> getAction() {
        return action;
    }

    public C getActionContext() {
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

    @Override
    public String toString() {
        return "LoggedActionViewModel{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", address='" + address + '\'' +
                ", action=" + action +
                ", actionContext=" + actionContext +
                ", newState='" + newState + '\'' +
                ", oldState='" + oldState + '\'' +
                ", project=" + project +
                ", version=" + version +
                ", page=" + page +
                ", subject=" + subject +
                ", createdAt=" + createdAt +
                '}';
    }
}
