package io.papermc.hangar.model;

import io.papermc.hangar.db.customtypes.RoleCategory;

public enum Role {

    HANGAR_ADMIN("Hangar_Admin", 1, RoleCategory.GLOBAL, Permission.All, "Hangar Admin", Color.RED),
    HANGAR_MOD("Hangar_Mod", 2, RoleCategory.GLOBAL, Permission.IsStaff.add(Permission.Reviewer).add(Permission.ModNotesAndFlags).add(Permission.SeeHidden), "Hangar Moderator", Color.AQUA),

    PAPER_LEADER("Paper_Leader", 3, RoleCategory.GLOBAL, Permission.None, "Paper Leader", Color.AMBER),
    TEAM_LEADER("Team_Leader", 4, RoleCategory.GLOBAL, Permission.None, "Team Leader", Color.AMBER),
    COMMUNITY_LEADER("Community_Leader", 5, RoleCategory.GLOBAL, Permission.None, "Community Leader", Color.AMBER),

    PAPER_STAFF("Paper_Staff", 6, RoleCategory.GLOBAL, Permission.None, "Paper Staff", Color.AMBER),
    PAPER_DEV("Paper_Developer", 7, RoleCategory.GLOBAL, Permission.None, "Paper Developer", Color.GREEN),

    HANGAR_DEV("Hangar_Dev", 8, RoleCategory.GLOBAL, Permission.ViewStats.add(Permission.ViewLogs).add(Permission.ViewHealth).add(Permission.ManualValueChanges), "Hangar Developer", Color.ORANGE),
    WEB_DEV("Web_Dev", 9, RoleCategory.GLOBAL, Permission.ViewLogs.add(Permission.ViewHealth), "Web Developer", Color.BLUE),

    DOCUMENTER("Documenter", 10, RoleCategory.GLOBAL, Permission.None, "Documenter", Color.AQUA),
    SUPPORT("Support", 11, RoleCategory.GLOBAL, Permission.None, "Support", Color.AQUA),
    CONTRIBUTOR("Contributor", 12, RoleCategory.GLOBAL, Permission.None, "Contributor", Color.GREEN),
    ADVISOR("Advisor", 13, RoleCategory.GLOBAL, Permission.None, "Advisor", Color.AQUA),

    STONE_DONOR("Stone_Donor", 14, RoleCategory.GLOBAL, Permission.None, "Stone Donor", Color.GRAY, 5L),
    QUARTZ_DONOR("Quartz_Donor",15, RoleCategory.GLOBAL, Permission.None, "Quartz Donor", Color.QUARTZ, 4L),
    IRON_DONOR("Iron_Donor",16, RoleCategory.GLOBAL, Permission.None, "Iron Donor", Color.SILVER, 3L),
    GOLD_DONOR("Gold_Donor",17, RoleCategory.GLOBAL, Permission.None, "Gold Donor", Color.GOLD, 2L),
    DIAMOND_DONOR("Diamond_Donor",18, RoleCategory.GLOBAL, Permission.None, "Diamond Donor", Color.LIGHTBLUE, 1L),

    PROJECT_SUPPORT("Project_Support", 22, RoleCategory.PROJECT, Permission.IsProjectMember, "Support", Color.TRANSPARENT),
    PROJECT_EDITOR("Project_Editor", 21, RoleCategory.PROJECT, Permission.EditPage.add(PROJECT_SUPPORT.getPermissions()), "Editor", Color.TRANSPARENT),
    PROJECT_DEVELOPER("Project_Developer", 20, RoleCategory.PROJECT, Permission.CreateVersion.add(Permission.EditVersion).add(Permission.EditTags).add(PROJECT_EDITOR.getPermissions()), "Developer", Color.TRANSPARENT),
    PROJECT_OWNER("Project_Owner", 19, RoleCategory.PROJECT, Permission.IsProjectOwner.add(Permission.EditApiKeys).add(Permission.DeleteProject).add(Permission.DeleteVersion).add(PROJECT_DEVELOPER.getPermissions()), "Owner", Color.TRANSPARENT, false),

    ORGANIZATION_SUPPORT("Organization_Support", 28, RoleCategory.ORGANIZATION, Permission.PostAsOrganization.add(Permission.IsOrganizationMember), "Support", Color.TRANSPARENT),
    ORGANIZATION_EDITOR("Organization_Editor", 27, RoleCategory.ORGANIZATION, PROJECT_EDITOR.permissions.add(ORGANIZATION_SUPPORT.permissions), "Editor", Color.TRANSPARENT),
    ORGANIZATION_DEVELOPER("Organization_Developer", 26, RoleCategory.ORGANIZATION, Permission.CreateProject.add(Permission.EditProjectSettings).add(PROJECT_DEVELOPER.permissions).add(ORGANIZATION_EDITOR.permissions), "Developer", Color.TRANSPARENT),
    ORGANIZATION_ADMIN("Organization_Admin", 25, RoleCategory.ORGANIZATION, Permission.EditApiKeys.add(Permission.ManageProjectMembers).add(Permission.EditOwnUserSettings).add(Permission.DeleteProject).add(Permission.DeleteVersion).add(ORGANIZATION_DEVELOPER.permissions), "Admin", Color.TRANSPARENT),
    ORGANIZATION_OWNER("Organization_Owner", 24, RoleCategory.ORGANIZATION, Permission.IsOrganizationOwner.add(PROJECT_OWNER.permissions).add(ORGANIZATION_ADMIN.permissions), "Owner", Color.PURPLE, false),
    ORGANIZATION("Organization", 23, RoleCategory.ORGANIZATION, ORGANIZATION_OWNER.permissions, "Organization", Color.PURPLE, false);

    private static final Role[] VALUES = values();

    private final String value;
    private final long roleId;
    private final RoleCategory category;
    private final Permission permissions;
    private final String title;
    private final Color color;
    private final boolean isAssignable;
    private Long rank = null;

    Role(String value, int roleId, RoleCategory category, Permission permissions, String title, Color color) {
        this(value, roleId, category, permissions, title, color, true);
    }

    Role(String value, int roleId, RoleCategory category, Permission permissions, String title, Color color, Long rank) {
        this(value, roleId, category, permissions, title, color);
        this.rank = rank;
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

    public long getRoleId() {
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

    public Long getRank() {
        return rank;
    }

    public static Role fromTitle(String title) {
        for (Role r : VALUES) {
            if (r.title.equals(title)) {
                return r;
            }
        }
        return null;
    }

    public static Role fromValue(String value) {
        for (Role r : values()) {
            if (r.value.equals(value)) {
                return r;
            }
        }
        return null;
    }

    public static Role fromId(long id) {
        for (Role r : VALUES) {
            if (r.roleId == id) {
                return r;
            }
        }
        return null;
    }
}
