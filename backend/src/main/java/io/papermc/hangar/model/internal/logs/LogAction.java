package io.papermc.hangar.model.internal.logs;

import io.papermc.hangar.db.customtypes.PGLoggedAction;
import io.papermc.hangar.model.db.log.LoggedActionTable;
import io.papermc.hangar.model.internal.logs.contexts.LogContext;
import io.papermc.hangar.model.internal.logs.contexts.OrganizationContext;
import io.papermc.hangar.model.internal.logs.contexts.PageContext;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.internal.logs.contexts.UserContext;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class LogAction<LC extends LogContext<? extends LoggedActionTable, LC>> {

    public static final Map<String, LogAction<? extends LogContext<? extends LoggedActionTable, ?>>> LOG_REGISTRY = new HashMap<>();

    // Projects
    public static final LogAction<ProjectContext> PROJECT_VISIBILITY_CHANGED = new LogAction<>(PGLoggedAction.PROJECT_VISIBILITY_CHANGED, "Project Visibility Changed");
    public static final LogAction<ProjectContext> PROJECT_RENAMED = new LogAction<>(PGLoggedAction.PROJECT_RENAMED, "Project Renamed");
    public static final LogAction<ProjectContext> PROJECT_FLAGGED = new LogAction<>(PGLoggedAction.PROJECT_FLAGGED, "Project Flagged");
    public static final LogAction<ProjectContext> PROJECT_SETTINGS_CHANGED = new LogAction<>(PGLoggedAction.PROJECT_SETTINGS_CHANGED, "Project Settings Changed");
    public static final LogAction<ProjectContext> PROJECT_ICON_CHANGED = new LogAction<>(PGLoggedAction.PROJECT_ICON_CHANGED, "Project Icon Changed");
    public static final LogAction<ProjectContext> PROJECT_FLAG_RESOLVED = new LogAction<>(PGLoggedAction.PROJECT_FLAG_RESOLVED, "Project Flag Resolved");

    public static final LogAction<ProjectContext> PROJECT_CHANNEL_CREATED = new LogAction<>(PGLoggedAction.PROJECT_CHANNEL_CREATED, "Project Channel Created");
    public static final LogAction<ProjectContext> PROJECT_CHANNEL_EDITED = new LogAction<>(PGLoggedAction.PROJECT_CHANNEL_EDITED, "Project Channel Edited");
    public static final LogAction<ProjectContext> PROJECT_CHANNEL_DELETED = new LogAction<>(PGLoggedAction.PROJECT_CHANNEL_DELETED, "Project Channel Deleted");

    public static final LogAction<ProjectContext> PROJECT_INVITES_SENT = new LogAction<>(PGLoggedAction.PROJECT_INVITES_SENT, "Project Invites Sent");
    public static final LogAction<ProjectContext> PROJECT_INVITE_DECLINED = new LogAction<>(PGLoggedAction.PROJECT_INVITE_DECLINED, "Project Invite Declined");
    public static final LogAction<ProjectContext> PROJECT_INVITE_UNACCEPTED = new LogAction<>(PGLoggedAction.PROJECT_INVITE_UNACCEPTED, "Project Invite Unaccepted");
    public static final LogAction<ProjectContext> PROJECT_MEMBER_ADDED = new LogAction<>(PGLoggedAction.PROJECT_MEMBER_ADDED, "Project Member Added");
    public static final LogAction<ProjectContext> PROJECT_MEMBERS_REMOVED = new LogAction<>(PGLoggedAction.PROJECT_MEMBERS_REMOVED, "Project Members Removed");
    public static final LogAction<ProjectContext> PROJECT_MEMBER_ROLES_CHANGED = new LogAction<>(PGLoggedAction.PROJECT_MEMBER_ROLES_CHANGED, "Project Member Roles Changed");

    // Pages
    public static final LogAction<PageContext> PROJECT_PAGE_CREATED = new LogAction<>(PGLoggedAction.PROJECT_PAGE_CREATED, "Project Page Created");
    public static final LogAction<PageContext> PROJECT_PAGE_DELETED = new LogAction<>(PGLoggedAction.PROJECT_PAGE_DELETED, "Project Page Deleted");
    public static final LogAction<PageContext> PROJECT_PAGE_EDITED = new LogAction<>(PGLoggedAction.PROJECT_PAGE_EDITED, "Project Page Edited");

    // Versions
    public static final LogAction<VersionContext> VERSION_VISIBILITY_CHANGED = new LogAction<>(PGLoggedAction.VERSION_VISIBILITY_CHANGED, "Version Visibility Changed");
    public static final LogAction<VersionContext> VERSION_DELETED = new LogAction<>(PGLoggedAction.VERSION_DELETED, "Version Deleted");
    public static final LogAction<VersionContext> VERSION_CREATED = new LogAction<>(PGLoggedAction.VERSION_CREATED, "Version Created");
    public static final LogAction<VersionContext> VERSION_DESCRIPTION_CHANGED = new LogAction<>(PGLoggedAction.VERSION_DESCRIPTION_CHANGED, "Version Description Changed");
    public static final LogAction<VersionContext> VERSION_REVIEW_STATE_CHANGED = new LogAction<>(PGLoggedAction.VERSION_REVIEW_STATE_CHANGED, "Version Review State Changed");

    public static final LogAction<VersionContext> VERSION_PLUGIN_DEPENDENCIES_ADDED = new LogAction<>(PGLoggedAction.VERSION_PLUGIN_DEPENDENCIES_ADDED, "Version Plugin Dependency Added");
    public static final LogAction<VersionContext> VERSION_PLUGIN_DEPENDENCIES_EDITED = new LogAction<>(PGLoggedAction.VERSION_PLUGIN_DEPENDENCIES_EDITED, "Version Plugin Dependency Edited");
    public static final LogAction<VersionContext> VERSION_PLUGIN_DEPENDENCIES_REMOVED = new LogAction<>(PGLoggedAction.VERSION_PLUGIN_DEPENDENCIES_REMOVED, "Version Plugin Dependency Removed");
    public static final LogAction<VersionContext> VERSION_PLATFORM_DEPENDENCIES_ADDED = new LogAction<>(PGLoggedAction.VERSION_PLATFORM_DEPENDENCIES_ADDED, "Version Platform Dependency Added");
    public static final LogAction<VersionContext> VERSION_PLATFORM_DEPENDENCIES_REMOVED = new LogAction<>(PGLoggedAction.VERSION_PLATFORM_DEPENDENCIES_REMOVED, "Version Platform Dependency Removed");

    // Users
    public static final LogAction<UserContext> USER_TAGLINE_CHANGED = new LogAction<>(PGLoggedAction.USER_TAGLINE_CHANGED, "User Tagline Changed");
    public static final LogAction<UserContext> USER_LOCKED = new LogAction<>(PGLoggedAction.USER_LOCKED, "User Locked");
    public static final LogAction<UserContext> USER_UNLOCKED = new LogAction<>(PGLoggedAction.USER_UNLOCKED, "User Unlocked");
    public static final LogAction<UserContext> USER_APIKEY_CREATED = new LogAction<>(PGLoggedAction.USER_APIKEY_CREATED, "User Apikey Created");
    public static final LogAction<UserContext> USER_APIKEY_DELETED = new LogAction<>(PGLoggedAction.USER_APIKEY_DELETED, "User Apikey Deleted");

    // Organizations
    // TODO create organization
    public static final LogAction<OrganizationContext> ORGANIZATION_INVITES_SENT = new LogAction<>(PGLoggedAction.ORGANIZATION_INVITES_SENT, "Organization Invites Sent");
    public static final LogAction<OrganizationContext> ORGANIZATION_INVITE_DECLINED = new LogAction<>(PGLoggedAction.ORGANIZATION_INVITE_DECLINED, "Organization Invite Declined");
    public static final LogAction<OrganizationContext> ORGANIZATION_MEMBER_ADDED = new LogAction<>(PGLoggedAction.ORGANIZATION_MEMBER_ADDED, "Organization Member Added");
    public static final LogAction<OrganizationContext> ORGANIZATION_MEMBERS_REMOVED = new LogAction<>(PGLoggedAction.ORGANIZATION_MEMBERS_REMOVED, "Organization Members Removed");
    public static final LogAction<OrganizationContext> ORGANIZATION_MEMBER_ROLES_CHANGED = new LogAction<>(PGLoggedAction.ORGANIZATION_MEMBER_ROLES_CHANGED, "Organization Member Roles Changed");


    private final PGLoggedAction pgLoggedAction;
    private final String name;

    private LogAction(final PGLoggedAction pgLoggedAction, final String name) {
        this.pgLoggedAction = pgLoggedAction;
        this.name = name;
        if (LOG_REGISTRY.containsKey(pgLoggedAction.getValue())) {
            throw new IllegalArgumentException(pgLoggedAction.getValue() + " already has a registered LogType");
        }
        LOG_REGISTRY.put(pgLoggedAction.getValue(), this);
    }

    public PGLoggedAction getPgLoggedAction() {
        return this.pgLoggedAction;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return "userActionLog.types." + this.name.replaceAll("\\s+", "");
    }

    public LoggedAction<LC> create(final LC context, final @NotNull String newState, final @NotNull String oldState) {
        return new LoggedAction<>(this, context, newState, oldState);
    }
}
