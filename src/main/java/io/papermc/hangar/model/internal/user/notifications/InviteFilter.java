package io.papermc.hangar.model.internal.user.notifications;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

@JsonFormat(shape = Shape.OBJECT)
public enum InviteFilter {
    ALL("all", "notification.invite.all"),
    PROJECTS("projects", "notification.invite.projects"),
    ORGANIZATIONS("organizations", "notification.invite.organizations");

    private final String name;
    private final String title;

    public static final InviteFilter[] VALUES = values();

    InviteFilter(String name, String title) {
        this.name = name;
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }
}
