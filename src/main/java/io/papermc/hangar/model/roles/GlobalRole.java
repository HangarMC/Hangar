package io.papermc.hangar.model.roles;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.Color;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.db.roles.GlobalRoleTable;
import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum GlobalRole implements Role<GlobalRoleTable> {

    HANGAR_ADMIN("Hangar_Admin", 1, Permission.All, "Hangar Admin", Color.RED),
    HANGAR_MOD("Hangar_Mod", 2, Permission.IsStaff.add(Permission.Reviewer).add(Permission.ModNotesAndFlags).add(Permission.SeeHidden), "Hangar Moderator", Color.AQUA),

    PAPER_LEADER("Paper_Leader", 3, Permission.None, "Paper Leader", Color.AMBER),
    TEAM_LEADER("Team_Leader", 4, Permission.None, "Team Leader", Color.AMBER),
    COMMUNITY_LEADER("Community_Leader", 5, Permission.None, "Community Leader", Color.AMBER),

    PAPER_STAFF("Paper_Staff", 6, Permission.None, "Paper Staff", Color.AMBER),
    PAPER_DEV("Paper_Developer", 7, Permission.None, "Paper Developer", Color.GREEN),

    HANGAR_DEV("Hangar_Dev", 8, Permission.ViewStats.add(Permission.ViewLogs).add(Permission.ViewHealth).add(Permission.ManualValueChanges), "Hangar Developer", Color.ORANGE),
    WEB_DEV("Web_Dev", 9, Permission.ViewLogs.add(Permission.ViewHealth), "Web Developer", Color.BLUE),

    DOCUMENTER("Documenter", 10, Permission.None, "Documenter", Color.AQUA),
    SUPPORT("Support", 11, Permission.None, "Support", Color.AQUA),
    CONTRIBUTOR("Contributor", 12, Permission.None, "Contributor", Color.GREEN),
    ADVISOR("Advisor", 13, Permission.None, "Advisor", Color.AQUA),

    STONE_DONOR("Stone_Donor", 14, Permission.None, "Stone Donor", Color.GRAY, 5L),
    QUARTZ_DONOR("Quartz_Donor",15, Permission.None, "Quartz Donor", Color.QUARTZ, 4L),
    IRON_DONOR("Iron_Donor",16, Permission.None, "Iron Donor", Color.SILVER, 3L),
    GOLD_DONOR("Gold_Donor",17, Permission.None, "Gold Donor", Color.GOLD, 2L),
    DIAMOND_DONOR("Diamond_Donor",18, Permission.None, "Diamond Donor", Color.LIGHTBLUE, 1L);

    private final String value;
    private final long roleId;
    private final Permission permissions;
    private final String title;
    private final Color color;
    private final Long rank;

    GlobalRole(String value, long roleId, Permission permissions, String title, Color color) {
        this(value, roleId, permissions, title, color, null);
    }

    GlobalRole(String value, long roleId, Permission permissions, String title, Color color, Long rank) {
        this.value = value;
        this.roleId = roleId;
        this.permissions = permissions;
        this.title = title;
        this.color = color;
        this.rank = rank;
        Role.registerRole(this);
    }

    @NotNull
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public long getRoleId() {
        return roleId;
    }

    @NotNull
    @Override
    public RoleCategory getRoleCategory() {
        return RoleCategory.GLOBAL;
    }

    @NotNull
    @Override
    public Permission getPermissions() {
        return permissions;
    }

    @NotNull
    @Override
    public String getTitle() {
        return title;
    }

    @NotNull
    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public boolean isAssignable() {
        return false;
    }

    @Nullable
    @Override
    public Long getRank() {
        return rank;
    }

    @NotNull
    @Override
    public GlobalRoleTable create(@Nullable Long principalId, long userId, boolean isAccepted) {
        return new GlobalRoleTable(userId, this);
    }

    @Override
    public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
        statement.setLong(position, roleId);
    }
}
