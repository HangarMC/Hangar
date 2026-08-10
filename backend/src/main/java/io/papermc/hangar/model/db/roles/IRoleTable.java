package io.papermc.hangar.model.db.roles;

public interface IRoleTable {

    long getUserId();

    boolean isAccepted();

    void setAccepted(boolean accepted);
}
