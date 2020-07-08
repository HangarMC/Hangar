package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets NamedPermission
 */
public enum NamedPermission {
    VIEW_PUBLIC_INFO("view_public_info", Permission.ViewPublicInfo),
    EDIT_OWN_USER_SETTINGS("edit_own_user_settings", Permission.EditOwnUserSettings),
    EDIT_API_KEYS("edit_api_keys", Permission.EditApiKeys),

    EDIT_SUBJECT_SETTINGS("edit_subject_settings", Permission.EditSubjectSettings),
    MANAGE_SUBJECT_MEMBERS("manage_subject_members", Permission.ManageSubjectMembers),
    IS_SUBJECT_OWNER("is_subject_owner", Permission.IsSubjectOwner),
    IS_SUBJECT_MEMBER("is_subject_member", Permission.IsSubjectMember),

    CREATE_PROJECT("create_project", Permission.CreateProject),
    EDIT_PAGE("edit_page", Permission.EditPage),
    DELETE_PROJECT("delete_project", Permission.DeleteProject),

    CREATE_VERSION("create_version", Permission.CreateVersion),
    EDIT_VERSION("edit_version", Permission.EditVersion),
    DELETE_VERSION("delete_version", Permission.DeleteVersion),
    EDIT_TAGS("edit_tags", Permission.EditTags),

    CREATE_ORGANIZATION("create_organization", Permission.CreateOrganization),
    POST_AS_ORGANIZATION("post_as_organization", Permission.PostAsOrganization),

    MOD_NOTES_AND_FLAGS("mod_notes_and_flags", Permission.ModNotesAndFlags),
    SEE_HIDDEN("see_hidden", Permission.SeeHidden),
    IS_STAFF("is_staff", Permission.IsStaff),
    REVIEWER("reviewer", Permission.Reviewer),

    VIEW_HEALTH("view_health", Permission.ViewHealth),
    VIEW_IP("view_ip", Permission.ViewIp),
    VIEW_STATS("view_stats", Permission.ViewStats),
    VIEW_LOGS("view_logs", Permission.ViewLogs),

    MANUAL_VALUE_CHANGES("manual_value_changes", Permission.ManualValueChanges),
    HARD_DELETE_PROJECT("hard_delete_project", Permission.HardDeleteProject),
    HARD_DELETE_VERSION("hard_delete_version", Permission.HardDeleteVersion),
    EDIT_ALL_USER_SETTINGS("edit_all_user_settings", Permission.EditAllUserSettings);

    private final String value;
    private final Permission permission;

    NamedPermission(String value, Permission permission) {
        this.value = value;
        this.permission = permission;
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

    public Permission getPermission() {
        return permission;
    }
}
