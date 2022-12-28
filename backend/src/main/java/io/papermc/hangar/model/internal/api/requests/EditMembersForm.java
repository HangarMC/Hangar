package io.papermc.hangar.model.internal.api.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.HttpStatus;

public class EditMembersForm<R extends Role<? extends ExtendedRoleTable<R, ?>>> {

    private final List<@Valid Member<R>> members;

    @JsonCreator
    public EditMembersForm(final List<Member<R>> members) {
        this.members = members;
    }

    public List<Member<R>> getMembers() {
        return this.members;
    }

    @Override
    public String toString() {
        return "EditMembersForm{" +
            "members=" + this.members +
            '}';
    }

    public static class Member<R extends Role<? extends ExtendedRoleTable<R, ?>>> {

        @NotBlank
        private final String name;

        // @el(root: Role)
        private final @Validate(SpEL = "#root.assignable") R role;

        @SuppressWarnings("unchecked")
        @JsonCreator
        public Member(final String name, final long roleId) {
            this.name = name;
            try {
                this.role = (R) Role.ID_ROLES.get(roleId);
            } catch (final ClassCastException e) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST);
            }
        }

        public String getName() {
            return this.name;
        }

        public R getRole() {
            return this.role;
        }

        @Override
        public String toString() {
            return "Member{" +
                ", name='" + this.name + '\'' +
                ", role=" + this.role +
                '}';
        }
    }
}
