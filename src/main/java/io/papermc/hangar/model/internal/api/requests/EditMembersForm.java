package io.papermc.hangar.model.internal.api.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import org.springframework.http.HttpStatus;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EditMembersForm<R extends Role<? extends ExtendedRoleTable<R, ?>>> {

    private final List<@Valid Member<R>> newInvitees;
    private final List<@Valid Member<R>> editedMembers;
    private final List<@Valid Member<R>> deletedMembers;

    @JsonCreator
    public EditMembersForm(List<Member<R>> members) {
        Predicate<Member<R>> editPredicate = Member::isEditing;
        newInvitees = members.stream().filter(Member::isNewMember).collect(Collectors.toList());
        editedMembers = members.stream().filter(editPredicate.and(Predicate.not(Member::isNewMember))).collect(Collectors.toList());
        deletedMembers = members.stream().filter(Member::isToDelete).collect(Collectors.toList());
    }

    public List<Member<R>> getNewInvitees() {
        return newInvitees;
    }

    public List<Member<R>> getEditedMembers() {
        return editedMembers;
    }

    public List<Member<R>> getDeletedMembers() {
        return deletedMembers;
    }

    @Override
    public String toString() {
        return "EditMembersForm{" +
                "newInvitees=" + newInvitees +
                ", editedMembers=" + editedMembers +
                ", deletedMembers=" + deletedMembers +
                '}';
    }

    public static class Member<R extends Role<? extends ExtendedRoleTable<R, ?>>> {

        private final boolean editing;
        private final boolean newMember;
        private final boolean toDelete;
        @NotBlank
        private final String name;
        @Validate(SpEL = "#root.isAssignable")
        private final R role;

        @SuppressWarnings("unchecked")
        @JsonCreator
        public Member(boolean editing, @JsonProperty("new") boolean newMember, boolean toDelete, String name, long roleId) {
            this.editing = editing;
            this.newMember = newMember;
            this.toDelete = toDelete;
            this.name = name;
            try {
                this.role = (R) Role.ID_ROLES.get(roleId);
            } catch (ClassCastException e) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST);
            }

        }

        public boolean isEditing() {
            return editing;
        }

        public boolean isNewMember() {
            return newMember;
        }

        public boolean isToDelete() {
            return toDelete;
        }

        public String getName() {
            return name;
        }

        public R getRole() {
            return role;
        }

        @Override
        public String toString() {
            return "Member{" +
                    "editing=" + editing +
                    ", newMember=" + newMember +
                    ", toDelete=" + toDelete +
                    ", name='" + name + '\'' +
                    ", role=" + role +
                    '}';
        }
    }

}
