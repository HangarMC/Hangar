package io.papermc.hangar.model.internal.logs;

import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.internal.logs.contexts.Context;
import io.papermc.hangar.model.internal.logs.viewmodels.LogPage;
import io.papermc.hangar.model.internal.logs.viewmodels.LogProject;
import io.papermc.hangar.model.internal.logs.viewmodels.LogSubject;
import io.papermc.hangar.model.internal.logs.viewmodels.LogVersion;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;

import java.net.InetAddress;
import java.time.OffsetDateTime;

public class HangarLoggedAction extends Model {

    private final Long userId;
    private final String userName;
    private final InetAddress address;
    private final LogAction<?> action;
    private final Context contextType;
    private final String newState;
    private final String oldState;
    private final LogProject project;
    private final LogVersion version;
    private final LogPage page;
    private final LogSubject subject;

    public HangarLoggedAction(OffsetDateTime createdAt, Long userId, String userName, InetAddress address, LogAction<?> action, @EnumByOrdinal Context contextType, String newState, String oldState, @Nested("p_") LogProject project, @Nested("pv_") LogVersion version, @Nested("pp_") LogPage page, @Nested("s_") LogSubject subject) {
        super(createdAt);
        this.userId = userId;
        this.userName = userName;
        this.address = address;
        this.action = action;
        this.contextType = contextType;
        this.newState = newState;
        this.oldState = oldState;
        this.project = project;
        this.version = version;
        this.page = page;
        this.subject = subject;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public InetAddress getAddress() {
        return address;
    }

    public LogAction<?> getAction() {
        return action;
    }

    public Context getContextType() {
        return contextType;
    }

    public String getNewState() {
        return newState;
    }

    public String getOldState() {
        return oldState;
    }

    public LogProject getProject() {
        return project;
    }

    public LogVersion getVersion() {
        return version;
    }

    public LogPage getPage() {
        return page;
    }

    public LogSubject getSubject() {
        return subject;
    }

    @Override
    public String toString() {
        return "HangarLoggedAction{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", address=" + address +
                ", action=" + action +
                ", context=" + contextType +
                ", newState='" + newState + '\'' +
                ", oldState='" + oldState + '\'' +
                ", project=" + project +
                ", version=" + version +
                ", page=" + page +
                ", subject=" + subject +
                "} " + super.toString();
    }
}
