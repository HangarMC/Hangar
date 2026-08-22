package io.papermc.hangar.model.internal.api.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.model.common.MemberPermissions;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public class EditMembersForm<M extends EditMembersForm.Member> {

    private final List<@Valid M> members;

    @JsonCreator
    public EditMembersForm(final List<M> members) {
        this.members = members;
    }

    public List<M> getMembers() {
        return this.members;
    }

    @Override
    public String toString() {
        return "EditMembersForm{" +
            "members=" + this.members +
            '}';
    }

    public static class Member {

        @NotBlank
        private final String name;
        // absent when removing a member, required otherwise -- checked where it is used
        @Size(max = MemberPermissions.MAX_TITLE_LENGTH)
        private final @Nullable String title;
        private final @Nullable Set<NamedPermission> permissions;

        @JsonCreator
        public Member(final String name, final @Nullable String title, final @Nullable Set<NamedPermission> permissions) {
            this.name = name;
            this.title = title;
            this.permissions = permissions;
        }

        public String getName() {
            return this.name;
        }

        public @Nullable String getTitle() {
            return this.title;
        }

        public @Nullable Set<NamedPermission> getPermissions() {
            return this.permissions;
        }

        public Permission asPermission() {
            if (this.permissions == null) {
                return Permission.None;
            }
            return this.permissions.stream().map(NamedPermission::getPermission).reduce(Permission::add).orElse(Permission.None);
        }

        @Override
        public String toString() {
            return "Member{" +
                "name='" + this.name + '\'' +
                ", title='" + this.title + '\'' +
                ", permissions=" + this.permissions +
                '}';
        }
    }

    // type helpders for typescript...
    public static class EditOrgMembersForm extends EditMembersForm<OrgMember> {
        public EditOrgMembersForm(final List<OrgMember> members) {
            super(members);
        }
    }

    public static class EditProjectMembersForm extends EditMembersForm<ProjectMember> {
        public EditProjectMembersForm(final List<ProjectMember> members) {
            super(members);
        }
    }

    public static class OrgMember extends Member {
        public OrgMember(final String name, final @Nullable String title, final @Nullable Set<NamedPermission> permissions) {
            super(name, title, permissions);
        }
    }

    public static class ProjectMember extends Member {
        public ProjectMember(final String name, final @Nullable String title, final @Nullable Set<NamedPermission> permissions) {
            super(name, title, permissions);
        }
    }
}
