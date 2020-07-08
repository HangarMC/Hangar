package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets NamedPermission
 */
public enum NamedPermission {
    VIEW_PUBLIC_INFO("view_public_info"),
    EDIT_OWN_USER_SETTINGS("edit_own_user_settings"),
    EDIT_API_KEYS("edit_api_keys"),
    EDIT_SUBJECT_SETTINGS("edit_subject_settings"),
    MANAGE_SUBJECT_MEMBERS("manage_subject_members"),
    IS_SUBJECT_OWNER("is_subject_owner"),
    CREATE_PROJECT("create_project"),
    EDIT_PAGE("edit_page"),
    DELETE_PROJECT("delete_project"),
    CREATE_VERSION("create_version"),
    EDIT_VERSION("edit_version"),
    DELETE_VERSION("delete_version"),
    EDIT_TAGS("edit_tags"),
    CREATE_ORGANIZATION("create_organization"),
    POST_AS_ORGANIZATION("post_as_organization"),
    MOD_NOTES_AND_FLAGS("mod_notes_and_flags"),
    SEE_HIDDEN("see_hidden"),
    IS_STAFF("is_staff"),
    REVIEWER("reviewer"),
    VIEW_HEALTH("view_health"),
    VIEW_IP("view_ip"),
    VIEW_STATS("view_stats"),
    VIEW_LOGS("view_logs"),
    MANUAL_VALUE_CHANGES("manual_value_changes"),
    HARD_DELETE_PROJECT("hard_delete_project"),
    HARD_DELETE_VERSION("hard_delete_version"),
    EDIT_ALL_USER_SETTINGS("edit_all_user_settings");

    private final String value;

    NamedPermission(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static NamedPermission fromValue(String text) {
        for (NamedPermission b : NamedPermission.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
