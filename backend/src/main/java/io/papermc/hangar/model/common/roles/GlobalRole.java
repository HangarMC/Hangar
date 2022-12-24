package io.papermc.hangar.model.common.roles;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.roles.GlobalRoleTable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum GlobalRole implements Role<GlobalRoleTable> {

    HANGAR_ADMIN("Hangar_Admin", 1, Permission.All, "Hangar Admin", Color.RED, 20),
    HANGAR_DEV("Hangar_Dev", 2, Permission.IsStaff.add(Permission.SeeHidden).add(Permission.ViewStats).add(Permission.ViewLogs).add(Permission.ViewHealth).add(Permission.ManualValueChanges), "Hangar Developer", Color.ORANGE, 30),
    HANGAR_MOD("Hangar_Mod", 3, Permission.IsStaff.add(Permission.Reviewer).add(Permission.ModNotesAndFlags).add(Permission.SeeHidden).add(Permission.RestoreVersion).add(Permission.RestoreProject), "Hangar Moderator", Color.AQUA, 40),

    PAPERMC_CORE("PaperMC_Core", 4, Permission.All, "PaperMC Core", Color.AMBER, 10),
    PAPERMC_STAFF("PaperMC_Staff", 5, Permission.IsStaff, "PaperMC Staff", Color.AMBER, 50),

    DUMMY("Dummy", 42, Permission.ViewPublicInfo, "Dummy", Color.CHARTREUSE, 42),

    ORGANIZATION("Organization", 100, OrganizationRole.ORGANIZATION_OWNER.getPermissions(), "Organization", Color.PURPLE);

    private static final GlobalRole[] VALUES = GlobalRole.values();

    private final String value;
    private final long roleId;
    private final Permission permissions;
    private final String title;
    private final Color color;
    private final Integer rank;

    GlobalRole(String value, long roleId, Permission permissions, String title, Color color) {
        this(value, roleId, permissions, title, color, null);
    }

    GlobalRole(String value, long roleId, Permission permissions, String title, Color color, Integer rank) {
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

    @Override
    public @Nullable Integer getRank() {
        return rank;
    }

    @NotNull
    @Override
    public GlobalRoleTable create(@Nullable Long ignored, long userId, boolean ignoredToo) {
        return new GlobalRoleTable(userId, this);
    }

    public static GlobalRole byApiValue(String apiValue) {
        for (GlobalRole value : values()) {
            if (value.value.endsWith(apiValue)) {
                return value;
            }
        }
        throw new IllegalArgumentException("No GlobalRole '" + apiValue + "'");
    }

    public static GlobalRole[] getValues() {
        return VALUES;
    }
}
