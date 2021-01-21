package io.papermc.hangar.modelold;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gets or Sets NamedPermission
 */
public enum NamedPermission {
    VIEW_PUBLIC_INFO("view_public_info", Permission.ViewPublicInfo, "ViewPublicInfo"),
    EDIT_OWN_USER_SETTINGS("edit_own_user_settings", Permission.EditOwnUserSettings, "EditOwnUserSettings"),
    EDIT_API_KEYS("edit_api_keys", Permission.EditApiKeys, "EditApiKeys"),

    EDIT_SUBJECT_SETTINGS("edit_subject_settings", Permission.EditSubjectSettings, "EditSubjectSettings"),
    MANAGE_SUBJECT_MEMBERS("manage_subject_members", Permission.ManageSubjectMembers, "ManageSubjectMembers"),
    IS_SUBJECT_OWNER("is_subject_owner", Permission.IsSubjectOwner, "IsSubjectOwner"),
    IS_SUBJECT_MEMBER("is_subject_member", Permission.IsSubjectMember, "IsSubjectMember"),

    CREATE_PROJECT("create_project", Permission.CreateProject, "CreateProject"),
    EDIT_PAGE("edit_page", Permission.EditPage, "EditPage"),
    DELETE_PROJECT("delete_project", Permission.DeleteProject, "DeleteProject"),

    CREATE_VERSION("create_version", Permission.CreateVersion, "CreateVersion"),
    EDIT_VERSION("edit_version", Permission.EditVersion, "EditVersion"),
    DELETE_VERSION("delete_version", Permission.DeleteVersion, "DeleteVersion"),
    EDIT_TAGS("edit_tags", Permission.EditTags, "EditTags"), // EDIT_CHANNELS

    CREATE_ORGANIZATION("create_organization", Permission.CreateOrganization, "CreateOrganization"),
    POST_AS_ORGANIZATION("post_as_organization", Permission.PostAsOrganization, "PostAsOrganization"),

    MOD_NOTES_AND_FLAGS("mod_notes_and_flags", Permission.ModNotesAndFlags, "ModNotesAndFlags"),
    SEE_HIDDEN("see_hidden", Permission.SeeHidden, "SeeHidden"),
    IS_STAFF("is_staff", Permission.IsStaff, "IsStaff"),
    REVIEWER("reviewer", Permission.Reviewer, "Reviewer"),

    VIEW_HEALTH("view_health", Permission.ViewHealth, "ViewHealth"),
    VIEW_IP("view_ip", Permission.ViewIp, "ViewIp"),
    VIEW_STATS("view_stats", Permission.ViewStats, "ViewStats"),
    VIEW_LOGS("view_logs", Permission.ViewLogs, "ViewLogs"),

    MANUAL_VALUE_CHANGES("manual_value_changes", Permission.ManualValueChanges, "ManualValueChanges"),
    HARD_DELETE_PROJECT("hard_delete_project", Permission.HardDeleteProject, "HardDeleteProject"),
    HARD_DELETE_VERSION("hard_delete_version", Permission.HardDeleteVersion, "HardDeleteVersion"),
    EDIT_ALL_USER_SETTINGS("edit_all_user_settings", Permission.EditAllUserSettings, "EditAllUserSettings");

    private final String value;
    private final Permission permission;
    private final String frontendName;

    NamedPermission(String value, Permission permission, String frontendName) {
        this.value = value;
        this.permission = permission;
        this.frontendName = frontendName;
    }

    public String getFrontendName() {
        return frontendName;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static NamedPermission fromValue(String text) {
        for (NamedPermission b : NamedPermission.values()) {
            if (b.value.equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static NamedPermission fromFrontendName(String text) {
        for (NamedPermission b : NamedPermission.values()) {
            if (b.frontendName.equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static List<NamedPermission> parseNamed(List<String> permStrings) {
        return permStrings.stream().map(s -> NamedPermission.valueOf(s.toUpperCase())).collect(Collectors.toList());
    }

    public Permission getPermission() {
        return permission;
    }
}
