package io.papermc.hangar.model.internal.logs;

import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.internal.logs.contexts.Context;
import io.papermc.hangar.model.internal.logs.viewmodels.LogPage;
import io.papermc.hangar.model.internal.logs.viewmodels.LogProject;
import io.papermc.hangar.model.internal.logs.viewmodels.LogSubject;
import io.papermc.hangar.model.internal.logs.viewmodels.LogVersion;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.Nested;

public class HangarLoggedAction extends Model {

    private final Long userId;
    private final String userName;
    private final LogAction<?> action;
    private final Context contextType;
    private final String newState;
    private final String oldState;
    private final LogProject project;
    private final LogVersion version;
    private final LogPage page;
    private final LogSubject subject;
    private InetAddress address;

    public HangarLoggedAction(final OffsetDateTime createdAt, final Long userId, final String userName, final InetAddress address, final LogAction<?> action, @EnumByOrdinal final Context contextType, final String newState, final String oldState, @Nested("p_") final LogProject project, @Nested("pv_") final LogVersion version, @Nested("pp_") final LogPage page, @Nested("s_") final LogSubject subject) {
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
        return this.userId;
    }

    public String getUserName() {
        return this.userName;
    }

    public @Nullable InetAddress getAddress() {
        return this.address;
    }

    public void hideAddress() {
        this.address = null;
    }

    public LogAction<?> getAction() {
        return this.action;
    }

    public Context getContextType() {
        return this.contextType;
    }

    public String getNewState() {
        return this.newState;
    }

    public String getOldState() {
        return this.oldState;
    }

    public LogProject getProject() {
        return this.project;
    }

    public LogVersion getVersion() {
        return this.version;
    }

    public LogPage getPage() {
        return this.page;
    }

    public LogSubject getSubject() {
        return this.subject;
    }

    @Override
    public String toString() {
        return "HangarLoggedAction{" +
            "userId=" + this.userId +
            ", userName='" + this.userName + '\'' +
            ", address=" + this.address +
            ", action=" + this.action +
            ", context=" + this.contextType +
            ", newState='" + this.newState + '\'' +
            ", oldState='" + this.oldState + '\'' +
            ", project=" + this.project +
            ", version=" + this.version +
            ", page=" + this.page +
            ", subject=" + this.subject +
            "} " + super.toString();
    }
}
