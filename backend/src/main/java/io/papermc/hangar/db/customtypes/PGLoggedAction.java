package io.papermc.hangar.db.customtypes;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;
import org.postgresql.util.PGobject;

public class PGLoggedAction extends PGobject {

    // Projects
    public static final PGLoggedAction PROJECT_VISIBILITY_CHANGED = new PGLoggedAction("project_visibility_changed");
    public static final PGLoggedAction PROJECT_RENAMED = new PGLoggedAction("project_renamed");
    public static final PGLoggedAction PROJECT_FLAGGED = new PGLoggedAction("project_flagged");
    public static final PGLoggedAction PROJECT_SETTINGS_CHANGED = new PGLoggedAction("project_settings_changed");
    public static final PGLoggedAction PROJECT_ICON_CHANGED = new PGLoggedAction("project_icon_changed");
    public static final PGLoggedAction PROJECT_FLAG_RESOLVED = new PGLoggedAction("project_flag_resolved");

    public static final PGLoggedAction PROJECT_CHANNEL_CREATED = new PGLoggedAction("project_channel_created");
    public static final PGLoggedAction PROJECT_CHANNEL_EDITED = new PGLoggedAction("project_channel_edited");
    public static final PGLoggedAction PROJECT_CHANNEL_DELETED = new PGLoggedAction("project_channel_deleted");

    public static final PGLoggedAction PROJECT_INVITES_SENT = new PGLoggedAction("project_invites_sent");
    public static final PGLoggedAction PROJECT_INVITE_DECLINED = new PGLoggedAction("project_invite_declined");
    public static final PGLoggedAction PROJECT_INVITE_UNACCEPTED = new PGLoggedAction("project_invite_unaccepted");
    public static final PGLoggedAction PROJECT_MEMBER_ADDED = new PGLoggedAction("project_member_added");
    public static final PGLoggedAction PROJECT_MEMBERS_REMOVED = new PGLoggedAction("project_members_removed");
    public static final PGLoggedAction PROJECT_MEMBER_ROLES_CHANGED = new PGLoggedAction("project_member_roles_changed");

    // Pages
    public static final PGLoggedAction PROJECT_PAGE_CREATED = new PGLoggedAction("project_page_created");
    public static final PGLoggedAction PROJECT_PAGE_DELETED = new PGLoggedAction("project_page_deleted");
    public static final PGLoggedAction PROJECT_PAGE_EDITED = new PGLoggedAction("project_page_edited");

    // Versions
    public static final PGLoggedAction VERSION_VISIBILITY_CHANGED = new PGLoggedAction("version_visibility_changed");
    public static final PGLoggedAction VERSION_DELETED = new PGLoggedAction("version_deleted");
    public static final PGLoggedAction VERSION_CREATED = new PGLoggedAction("version_created");
    public static final PGLoggedAction VERSION_DESCRIPTION_CHANGED = new PGLoggedAction("version_description_changed");
    public static final PGLoggedAction VERSION_REVIEW_STATE_CHANGED = new PGLoggedAction("version_review_state_changed");

    public static final PGLoggedAction VERSION_PLUGIN_DEPENDENCIES_ADDED = new PGLoggedAction("version_plugin_dependencies_added");
    public static final PGLoggedAction VERSION_PLUGIN_DEPENDENCIES_EDITED = new PGLoggedAction("version_plugin_dependencies_edited");
    public static final PGLoggedAction VERSION_PLUGIN_DEPENDENCIES_REMOVED = new PGLoggedAction("version_plugin_dependencies_removed");
    public static final PGLoggedAction VERSION_PLATFORM_DEPENDENCIES_ADDED = new PGLoggedAction("version_platform_dependencies_added");
    public static final PGLoggedAction VERSION_PLATFORM_DEPENDENCIES_REMOVED = new PGLoggedAction("version_platform_dependencies_removed");

    // Users
    public static final PGLoggedAction USER_TAGLINE_CHANGED = new PGLoggedAction("user_tagline_changed");
    public static final PGLoggedAction USER_LOCKED = new PGLoggedAction("user_locked");
    public static final PGLoggedAction USER_UNLOCKED = new PGLoggedAction("user_unlocked");
    public static final PGLoggedAction USER_APIKEY_CREATED = new PGLoggedAction("user_apikey_created");
    public static final PGLoggedAction USER_APIKEY_DELETED = new PGLoggedAction("user_apikey_deleted");

    // Organizations
    public static final PGLoggedAction ORGANIZATION_INVITES_SENT = new PGLoggedAction("organization_invites_sent");
    public static final PGLoggedAction ORGANIZATION_INVITE_DECLINED = new PGLoggedAction("organization_invite_declined");
    public static final PGLoggedAction ORGANIZATION_INVITE_UNACCEPTED = new PGLoggedAction("organization_invite_unaccepted");
    public static final PGLoggedAction ORGANIZATION_MEMBER_ADDED = new PGLoggedAction("organization_member_added");
    public static final PGLoggedAction ORGANIZATION_MEMBERS_REMOVED = new PGLoggedAction("organization_members_removed");
    public static final PGLoggedAction ORGANIZATION_MEMBER_ROLES_CHANGED = new PGLoggedAction("organization_member_roles_changed");

    private String value;

    public PGLoggedAction(final String value) {
        this.setType("logged_action_type");
        this.value = value;
    }

    @Override
    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;

        final PGLoggedAction that = (PGLoggedAction) obj;
        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (this.value != null ? this.value.hashCode() : 0);
        return result;
    }
}
