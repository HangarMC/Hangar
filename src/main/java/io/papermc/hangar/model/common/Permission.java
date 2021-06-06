package io.papermc.hangar.model.common;

import com.fasterxml.jackson.annotation.JsonValue;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    public static final Permission IsProjectOwner = new Permission(IsSubjectOwner.value | EditProjectSettings.value | ManageProjectMembers.value | IsProjectMember.value);

    public static final Permission CreateVersion = new Permission(1L << 12);
    public static final Permission EditVersion = new Permission(1L << 13);
    public static final Permission DeleteVersion = new Permission(1L << 14);
    public static final Permission EditTags = new Permission(1L << 15);

    public static final Permission CreateOrganization = new Permission(1L << 20);
    public static final Permission PostAsOrganization = new Permission(1L << 21);
    public static final Permission EditOrganizationSettings = new Permission(EditSubjectSettings.value);
    public static final Permission ManageOrganizationMembers = new Permission(ManageSubjectMembers.value);
    public static final Permission IsOrganizationMember = new Permission(IsProjectMember.value);
    public static final Permission IsOrganizationOwner = new Permission(IsProjectOwner.value);

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

    private long value;

    @JdbiConstructor
    public Permission(long value) {
        this.value = value;
    }

    public Permission() { }

    public void setValue(long value) {
        this.value = value;
    }

    public Permission add(Permission other) {
        return new Permission(value | other.value);
    }

    public Permission intersect(Permission other) {
        return new Permission(value & other.value);
    }

    public Permission remove(Permission other) {
        return new Permission(value & ~other.value);
    }

    public Permission toggle(Permission other) {
        return new Permission(value ^ other.value);
    }

    /**
     * Check if permission has all of another permission
     * @param other another permission
     * @return true if permission has all of other permission <b>OR</b> other permission is {@link #None}
     */
    public boolean has(Permission other) {
        return (value & other.value) == other.value;
    }

    public boolean hasAll(NamedPermission[] perms) {
        for (NamedPermission perm : perms) {
            if (!has(perm.getPermission())) {
                return false;
            }
        }
        return true;
    }

    public boolean isNone() {
        return value == 0;
    }
    @JsonValue
    public String toBinString() {
        return Long.toBinaryString(value);
    }

    public List<NamedPermission> toNamed() {
        return Arrays.stream(NamedPermission.values()).filter(perm -> has(perm.getPermission())).collect(Collectors.toUnmodifiableList());
    }

    public long getValue() {
        return value;
    }

    @Override
    public int compareTo(Permission o) {
        return (int) (value - o.value);
    }

    public static Permission fromLong(long value) {
        return new Permission(value);
    }

    public static Permission fromBinString(String binString) {
        if (binString == null) return null;
        return new Permission(Long.parseLong(binString, 2));
    }

    @Override
    public String toString() {
        return toNamed().toString();
    }

    @Override
    public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
        statement.setLong(position, this.value);
    }
}
