package io.papermc.hangar.model.common;

import com.fasterxml.jackson.annotation.JsonValue;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jdbi.v3.core.statement.StatementContext;

public class Permission implements Comparable<Permission>, Argument {

    public static final Permission None = new Permission(0);
    public static final Permission All = new Permission(0XFFFFFFFFFFFFFFFL);

    @SuppressWarnings("PointlessBitwiseExpression")
    public static final Permission ViewPublicInfo = new Permission(1L << 0);
    public static final Permission EditOwnUserSettings = new Permission(1L << 1);
    public static final Permission EditApiKeys = new Permission(1L << 2);

    public static final Permission EditSubjectSettings = new Permission(1L << 4);
    public static final Permission ManageSubjectMembers = new Permission(1L << 5);
    public static final Permission IsSubjectOwner = new Permission(1L << 6);
    public static final Permission IsSubjectMember = new Permission(1L << 7);

    public static final Permission CreateProject = new Permission(1L << 8);
    public static final Permission EditPage = new Permission(1L << 9);
    public static final Permission DeleteProject = new Permission(1L << 10);
    public static final Permission EditProjectSettings = new Permission(EditSubjectSettings.value);
    public static final Permission ManageProjectMembers = new Permission(ManageSubjectMembers.value);
    public static final Permission IsProjectMember = new Permission(IsSubjectMember.value);
    public static final Permission IsProjectAdmin = new Permission(EditProjectSettings.value | ManageProjectMembers.value | IsProjectMember.value);
    public static final Permission IsProjectOwner = new Permission(IsSubjectOwner.value | IsProjectAdmin.value | DeleteProject.value);

    public static final Permission CreateVersion = new Permission(1L << 12);
    public static final Permission EditVersion = new Permission(1L << 13);
    public static final Permission DeleteVersion = new Permission(1L << 14);
    public static final Permission EditTags = new Permission(1L << 15);

    public static final Permission CreateOrganization = new Permission(1L << 20);
    public static final Permission PostAsOrganization = new Permission(1L << 21);
    public static final Permission DeleteOrganization = new Permission(1L << 22);
    public static final Permission EditOrganizationSettings = new Permission(EditSubjectSettings.value);
    public static final Permission ManageOrganizationMembers = new Permission(ManageSubjectMembers.value);
    public static final Permission IsOrganizationMember = new Permission(IsProjectMember.value);
    public static final Permission IsOrganizationAdmin = new Permission(EditOrganizationSettings.value | ManageOrganizationMembers.value | IsOrganizationMember.value);
    public static final Permission IsOrganizationOwner = new Permission(IsProjectOwner.value | DeleteOrganization.value | IsOrganizationAdmin.value);

    public static final Permission ModNotesAndFlags = new Permission(1L << 24);
    public static final Permission SeeHidden = new Permission(1L << 25);
    public static final Permission IsStaff = new Permission(1L << 26);
    public static final Permission Reviewer = new Permission(1L << 27);

    public static final Permission ViewHealth = new Permission(1L << 32);
    public static final Permission ViewIp = new Permission(1L << 33);
    public static final Permission ViewStats = new Permission(1L << 34);
    public static final Permission ViewLogs = new Permission(1L << 35);

    public static final Permission ManualValueChanges = new Permission(1L << 40);
    public static final Permission HardDeleteProject = new Permission(1L << 41);
    public static final Permission HardDeleteVersion = new Permission(1L << 42);
    public static final Permission EditAllUserSettings = new Permission(1L << 43);
    public static final Permission RestoreVersion = new Permission(1L << 44);
    public static final Permission RestoreProject = new Permission(1L << 45);

    private final long value;

    @JdbiConstructor
    public Permission(final long value) {
        this.value = value;
    }

    public Permission add(final Permission other) {
        return new Permission(this.value | other.value);
    }

    public Permission intersect(final Permission other) {
        return new Permission(this.value & other.value);
    }

    public Permission remove(final Permission other) {
        return new Permission(this.value & ~other.value);
    }

    public Permission toggle(final Permission other) {
        return new Permission(this.value ^ other.value);
    }

    /**
     * Check if permission has all of another permission
     *
     * @param other another permission
     * @return true if permission has all of other permission <b>OR</b> other permission is {@link #None}
     */
    public boolean has(final Permission other) {
        return (this.value & other.value) == other.value;
    }

    public boolean hasAll(final NamedPermission[] perms) {
        for (final NamedPermission perm : perms) {
            if (!this.has(perm.getPermission())) {
                return false;
            }
        }
        return true;
    }

    public boolean isNone() {
        return this.value == 0;
    }

    @JsonValue
    public String toBinString() {
        return Long.toBinaryString(this.value);
    }

    public List<NamedPermission> toNamed() {
        return Arrays.stream(NamedPermission.values()).filter(perm -> this.has(perm.getPermission())).toList();
    }

    public long getValue() {
        return this.value;
    }

    @Override
    public int compareTo(final Permission o) {
        return (int) (this.value - o.value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final Permission that = (Permission) o;
        return this.value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }

    public static Permission fromLong(final long value) {
        return new Permission(value);
    }

    public static Permission fromBinString(final String binString) {
        if (binString == null) return null;
        return new Permission(Long.parseLong(binString, 2));
    }

    @Override
    public String toString() {
        return this.toNamed().toString();
    }

    @Override
    public void apply(final int position, final PreparedStatement statement, final StatementContext ctx) throws SQLException {
        statement.setLong(position, this.value);
    }
}
