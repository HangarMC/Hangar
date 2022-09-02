package io.papermc.hangar.model.internal.api.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import org.springframework.http.HttpStatus;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

public class EditMembersForm<R extends Role<? extends ExtendedRoleTable<R, ?>>> {

    private final List<@Valid Member<R>> members;

    @JsonCreator
    public EditMembersForm(List<Member<R>> members) {
        this.members = members;
    }

    public List<Member<R>> getMembers() {
        return members;
    }

    @Override
    public String toString() {
        return "EditMembersForm{" +
            "members=" + members +
            '}';
    }

    public static class Member<R extends Role<? extends ExtendedRoleTable<R, ?>>> {

        @NotBlank
        private final String name;
        @Validate(SpEL = "#root.isAssignable")
        private final R role;

        @SuppressWarnings("unchecked")
        @JsonCreator
        public Member(String name, long roleId) {
            this.name = name;
            try {
                this.role = (R) Role.ID_ROLES.get(roleId);
            } catch (ClassCastException e) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST);
            }
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
                ", name='" + name + '\'' +
                ", role=" + role +
                '}';
        }
    }
}
