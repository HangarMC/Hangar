package io.papermc.hangar.model.db.log;

import io.papermc.hangar.db.customtypes.PGLoggedAction;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.internal.logs.LoggedAction;
import java.net.InetAddress;

public abstract class LoggedActionTable extends Table {

    private final long userId;
    private final InetAddress address;
    private final PGLoggedAction action;
    private final String newState;
    private final String oldState;

    protected LoggedActionTable(final long userId, final InetAddress address, final LoggedAction<?> action) {
        this.userId = userId;
        this.address = address;
        this.action = action.getType().getPgLoggedAction();
        this.newState = action.getNewState();
        this.oldState = action.getOldState();
    }

    public long getUserId() {
        return this.userId;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public PGLoggedAction getAction() {
        return this.action;
    }

    public String getNewState() {
        return this.newState;
    }

    public String getOldState() {
        return this.oldState;
    }

    @Override
    public String toString() {
        return "UserLoggedAction{" +
            "userId=" + this.userId +
            ", address=" + this.address +
            ", action=" + this.action +
            ", newState='" + this.newState + '\'' +
            ", oldState='" + this.oldState + '\'' +
            "} " + super.toString();
    }
}
