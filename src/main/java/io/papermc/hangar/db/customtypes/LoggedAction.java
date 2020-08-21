package io.papermc.hangar.db.customtypes;

import org.postgresql.util.PGobject;

import java.util.Objects;

public class LoggedAction extends PGobject {

    public static final LoggedAction PROJECT_VISIBILITY_CHANGE = new LoggedAction("project_visibility_change");
    public static final LoggedAction PROJECT_RENAMED = new LoggedAction("project_renamed");
    public static final LoggedAction PROJECT_FLAGGED = new LoggedAction("project_flagged");
    public static final LoggedAction PROJECT_SETTINGS_CHANGED = new LoggedAction("project_settings_changed");
    public static final LoggedAction PROJECT_MEMBER_REMOVED = new LoggedAction("project_member_removed");
    public static final LoggedAction PROJECT_ICON_CHANGED = new LoggedAction("project_icon_changed");
    public static final LoggedAction PROJECT_PAGE_EDITED = new LoggedAction("project_page_edited");
    public static final LoggedAction PROJECT_FLAG_RESOLVED = new LoggedAction("project_flag_resolved");
    public static final LoggedAction VERSION_DELETED = new LoggedAction("version_deleted");
    public static final LoggedAction VERSION_UPLOADED = new LoggedAction("version_uploaded");
    public static final LoggedAction VERSION_DESCRIPTION_CHANGED = new LoggedAction("version_description_changed");
    public static final LoggedAction VERSION_REVIEW_STATE_CHANGED = new LoggedAction("version_review_state_changed");
    public static final LoggedAction USER_TAGLINE_CHANGED = new LoggedAction("user_tagline_changed");

    private String value;

    public LoggedAction(String value) {
        setType("logged_action_type");
        this.value = value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;

        LoggedAction that = (LoggedAction) obj;
        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
