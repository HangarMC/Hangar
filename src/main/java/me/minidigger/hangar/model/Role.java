package me.minidigger.hangar.model;

import me.minidigger.hangar.db.customtypes.RoleCategory;

import static me.minidigger.hangar.db.customtypes.RoleCategory.*;
import static me.minidigger.hangar.model.Color.*;
import static me.minidigger.hangar.model.Permission.*;

public enum Role {

    HANGAR_ADMIN("Hangar_Admin", 1, GLOBAL, All, "Hangar Admin", RED),
    HANGAR_MOD("Hangar_Mod", 2, GLOBAL, IsStaff.add(Reviewer).add(ModNotesAndFlags).add(SeeHidden), "Hangar Moderator", AQUA),

    PAPER_LEADER("Paper_Leader", 3, GLOBAL, None, "Paper Leader", AMBER),
    TEAM_LEADER("Team_Leader", 4, GLOBAL, None, "Team Leader", AMBER),
    COMMUNITY_LEADER("Community_Leader", 5, GLOBAL, None, "Community Leader", AMBER),

    PAPER_STAFF("Paper_Staff", 6, GLOBAL, None, "Paper Staff", AMBER),
    PAPER_DEV("Paper_Developer", 7, GLOBAL, None, "Paper Developer", GREEN),

    HANGAR_DEV("Hangar_Dev", 8, GLOBAL, ViewStats.add(ViewLogs).add(ViewHealth).add(ManualValueChanges), "Hangar Developer", ORANGE),
    WEB_DEV("Web_Dev", 9, GLOBAL, ViewLogs.add(ViewHealth), "Web Developer", BLUE),

    DOCUMENTER("Documenter", 10, GLOBAL, None, "Documenter", AQUA),
    SUPPORT("Support", 10, GLOBAL, None, "Support", AQUA),
    CONTRIBUTOR("Contributor", 10, GLOBAL, None, "Contributor", GREEN),
    ADVISOR("Advisor", 10, GLOBAL, None, "Advisor", AQUA),

    STONE_DONOR("Stone_Donor", 14, GLOBAL, None, "Stone Donor", GRAY),
    QUARTZ_DONOR("Quartz_Donor",15, GLOBAL, None, "Quartz Donor", QUARTZ),
    IRON_DONOR("Iron_Donor",16, GLOBAL, None, "Iron Donor", SILVER),
    GOLD_DONOR("Gold_Donor",17, GLOBAL, None, "Gold Donor", GOLD),
    DIAMOND_DONOR("Diamond_Donor",18, GLOBAL, None, "Diamond Donor", LIGHTBLUE),

    PROJECT_OWNER("Project_Owner", 19, PROJECT, IsProjectOwner.add(EditApiKeys).add(DeleteProject).add(DeleteVersion), "Owner", TRANSPARENT, false),
    PROJECT_SUPPORT("Project_Support", 22, PROJECT, IsProjectMember, "Support", TRANSPARENT),
    PROJECT_EDITOR("Project_Editor", 21, PROJECT, EditPage.add(PROJECT_SUPPORT.getPermissions()), "Editor", TRANSPARENT),
    PROJECT_DEVELOPER("Project_Developer", 20, PROJECT, CreateVersion.add(EditVersion).add(PROJECT_EDITOR.getPermissions()), "Developer", TRANSPARENT),

    ORGANIZATION_SUPPORT("Organization_Support", 28, RoleCategory.ORGANIZATION, PostAsOrganization.add(IsOrganizationMember), "Support", TRANSPARENT),
    ORGANIZATION_EDITOR("Organization_Editor", 27, RoleCategory.ORGANIZATION, PROJECT_EDITOR.permissions.add(ORGANIZATION_SUPPORT.permissions), "Editor", TRANSPARENT),
    ORGANIZATION_DEVELOPER("Organization_Developer", 26, RoleCategory.ORGANIZATION, CreateProject.add(EditProjectSettings).add(PROJECT_DEVELOPER.permissions).add(ORGANIZATION_EDITOR.permissions), "Developer", TRANSPARENT),
    ORGANIZATION_ADMIN("Organization_Admin", 25, RoleCategory.ORGANIZATION, EditApiKeys.add(ManageProjectMembers).add(EditOwnUserSettings).add(DeleteProject).add(DeleteVersion).add(ORGANIZATION_DEVELOPER.permissions), "Admin", TRANSPARENT),
    ORGANIZATION_OWNER("Organization_Owner", 24, RoleCategory.ORGANIZATION, IsOrganizationOwner.add(PROJECT_OWNER.permissions).add(ORGANIZATION_ADMIN.permissions), "Owner", PURPLE, false),
    ORGANIZATION("Organization", 23, RoleCategory.ORGANIZATION, ORGANIZATION_OWNER.permissions, "Organization", PURPLE, false);

    private String value;
    private int roleId;
    private RoleCategory category;
    private Permission permissions;
    private String title;
    private Color color;
    private boolean isAssignable;

    Role(String value, int roleId, RoleCategory category, Permission permissions, String title, Color color) {
        this(value, roleId, category, permissions, title, color, true);
    }

    Role(String value, int roleId, RoleCategory category, Permission permissions, String title, Color color, boolean isAssignable) {
        this.value = value;
        this.roleId = roleId;
        this.category = category;
        this.permissions = permissions;
        this.title = title;
        this.color = color;
        this.isAssignable = isAssignable;
    }

    public String getValue() {
        return value;
    }

    public int getRoleId() {
        return roleId;
    }

    public RoleCategory getCategory() {
        return category;
    }

    public Permission getPermissions() {
        return permissions;
    }

    public String getTitle() {
        return title;
    }

    public Color getColor() {
        return color;
    }

    public boolean isAssignable() {
        return isAssignable;
    }
}
