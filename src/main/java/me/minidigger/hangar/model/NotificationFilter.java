package me.minidigger.hangar.model;

import me.minidigger.hangar.db.model.NotificationsTable;

import java.util.List;
import java.util.function.Predicate;

public enum NotificationFilter {
    UNREAD(0, "unread", "notification.empty.unread", "notification.unread", "n.read = false"),
    READ(1, "read", "notification.empty.read", "notification.read", "n.read = true"),
    ALL(2, "all", "notification.empty.all", "notification.all", "true");

    private final long value;
    private final String name;
    private final String emptyMessage;
    private final String title;
    private final String filter;

    NotificationFilter(long value, String name, String emptyMessage, String title, String filter) {
        this.value = value;
        this.name = name;
        this.emptyMessage = emptyMessage;
        this.title = title;
        this.filter = filter;
    }

    public long getValue() {
        return value;
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
