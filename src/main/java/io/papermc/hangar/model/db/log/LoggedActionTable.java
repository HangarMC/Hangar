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

    public LoggedActionTable(long userId, InetAddress address, LoggedAction<?> action) {
        this.userId = userId;
        this.address = address;
        this.action = action.getType().getPgLoggedAction();
        this.newState = action.getNewState();
        this.oldState = action.getOldState();
    }

    public long getUserId() {
        return userId;
    }

    public InetAddress getAddress() {
        return address;
    }

    public PGLoggedAction getAction() {
        return action;
    }

    public String getNewState() {
        return newState;
    }

    public String getOldState() {
        return oldState;
    }

    @Override
    public String toString() {
        return "UserLoggedAction{" +
                "userId=" + userId +
                ", address=" + address +
                ", action=" + action +
                ", newState='" + newState + '\'' +
                ", oldState='" + oldState + '\'' +
                "} " + super.toString();
    }
}
