package io.papermc.hangar.model.common.roles;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.roles.GlobalRoleTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum GlobalRole implements Role<GlobalRoleTable> {

    HANGAR_ADMIN("Hangar_Admin", 1, Permission.All, "Hangar Admin", Color.RED, 10),
    HANGAR_DEV("Hangar_Dev", 2, Permission.IsStaff.add(Permission.SeeHidden).add(Permission.ViewStats).add(Permission.ViewLogs).add(Permission.ViewHealth), "Hangar Developer", Color.ORANGE, 30),
    HANGAR_MOD("Hangar_Mod", 3, Permission.IsStaff.add(Permission.Reviewer).add(Permission.DeleteVersion).add(Permission.DeleteProject).add(Permission.RestoreVersion).add(Permission.RestoreProject), "Hangar Moderator", Color.AQUA, 40),

    PAPERMC_CORE("PaperMC_Core", 4, Permission.All, "PaperMC Core", Color.AMBER, 0),
    PAPERMC_STAFF("PaperMC_Staff", 5, Permission.IsStaff.add(Permission.ModNotesAndFlags).add(Permission.SeeHidden), "PaperMC Staff", Color.AMBER, 50),
    PAPERMC_CM("PaperMC_CM", 6, Permission.All.remove(Permission.ManualValueChanges), "Community Manager", Color.AMBER, 20),

    DUMMY("Dummy", 42, Permission.ViewPublicInfo, "Dummy", Color.CHARTREUSE, 42),

    ORGANIZATION("Organization", 100, OrganizationRole.ORGANIZATION_OWNER.getPermissions(), "Organization", Color.PURPLE);

    private final String value;
    private final long roleId;
    private final Permission permissions;
    private final String title;
    private final Color color;
    private final Integer rank;

    GlobalRole(final String value, final long roleId, final Permission permissions, final String title, final Color color) {
        this(value, roleId, permissions, title, color, null);
    }

    GlobalRole(final String value, final long roleId, final Permission permissions, final String title, final Color color, final Integer rank) {
        this.value = value;
        this.roleId = roleId;
        this.permissions = permissions;
        this.title = title;
        this.color = color;
        this.rank = rank;
        Role.registerRole(this);
    }

    @Override
    public @NotNull String getValue() {
        return this.value;
    }

    @Override
    public long getRoleId() {
        return this.roleId;
    }

    @Override
    public @NotNull RoleCategory getRoleCategory() {
        return RoleCategory.GLOBAL;
    }

    @Override
    public @NotNull Permission getPermissions() {
        return this.permissions;
    }

    @Override
    public @NotNull String getTitle() {
        return this.title;
    }

    @Override
    public @NotNull Color getColor() {
        return this.color;
    }

    @Override
    public boolean isAssignable() {
        return false;
    }

    @Override
    public @Nullable Integer rank() {
        return this.rank;
    }

    @Override
    public @NotNull GlobalRoleTable create(final @Nullable Long ignored, final @Nullable UUID principalUuid, final long userId, final boolean ignoredToo) {
        return new GlobalRoleTable(userId, this);
    }

    public static GlobalRole byApiValue(final String apiValue) {
        for (final GlobalRole value : values()) {
            if (value.value.endsWith(apiValue)) {
                return value;
            }
        }
        throw new IllegalArgumentException("No GlobalRole '" + apiValue + "'");
    }
}
