package me.minidigger.hangar.model;

import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.viewhelpers.UserRole;
import me.minidigger.hangar.service.RoleService;

import java.util.List;

public enum InviteFilter {
    ALL(0, "all", "notification.invite.all"),
    PROJECTS(1, "projects", "notification.invite.projects"),
    ORGANIZATIONS(2, "organizations", "notification.invite.organizations");

    private final long value;
    private final String name;
    private final String title;

    InviteFilter(long value, String name, String title) {
        this.value = value;
        this.name = name;
        this.title = title;
    }

    public long getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }
}
