package io.papermc.hangar.modelold;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

@JsonFormat(shape = Shape.OBJECT)
public enum NotificationFilter {
    UNREAD("unread", "notification.empty.unread", "notification.unread", "n.read = false"),
    READ( "read", "notification.empty.read", "notification.read", "n.read = true"),
    ALL( "all", "notification.empty.all", "notification.all", "true");

    private final String name;
    private final String emptyMessage;
    private final String title;
    private final String filter;

    public static final NotificationFilter[] VALUES = values();

    NotificationFilter(String name, String emptyMessage, String title, String filter) {
        this.name = name;
        this.emptyMessage = emptyMessage;
        this.title = title;
        this.filter = filter;
    }

    public String getName() {
        return name;
    }

    public String getEmptyMessage() {
        return emptyMessage;
    }

    public String getTitle() {
        return title;
    }

    public String getFilter() {
        return filter;
    }
}
