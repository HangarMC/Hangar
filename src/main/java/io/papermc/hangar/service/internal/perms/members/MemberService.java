package io.papermc.hangar.service.internal.perms.members;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.members.MembersDAO;
import io.papermc.hangar.db.dao.internal.table.roles.IRolesDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.members.MemberTable;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import io.papermc.hangar.model.internal.api.requests.EditMembersForm.Member;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.internal.perms.roles.RoleService;
import io.papermc.hangar.service.internal.users.notifications.JoinableNotificationService;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

public abstract class MemberService<
        R extends Role<RT>,
        RT extends ExtendedRoleTable<R>,
        RD extends IRolesDAO<RT>,
        S extends RoleService<RT, R, RD>,
        J extends Table & Named,
        JNS extends JoinableNotificationService<RT, J>,
        MD extends MembersDAO<MT>,
        MT extends MemberTable
        > extends HangarService {

    @Autowired
    private HangarDao<UserDAO> userDAO;

    private final S roleService;
    private final MD membersDao;
    private final JNS joinableNotificationService;
    private final MemberTableConstructor<MT> constructor;
    private final String errorPrefix;

    protected MemberService(S roleService, MD membersDao, JNS joinableNotificationService, MemberTableConstructor<MT> constructor, String errorPrefix) {
        this.roleService = roleService;
        this.membersDao = membersDao;
        this.joinableNotificationService = joinableNotificationService;
        this.constructor = constructor;
        this.errorPrefix = errorPrefix;
    }

    @Nullable
    public RT addNewAcceptedByDefaultMember(RT newRoleTable) {
        if (!newRoleTable.isAccepted()) {
            throw new IllegalArgumentException("Should only be used by accepted roles");
        }
        MT existingMember = membersDao.getMemberTable(newRoleTable.getPrincipalId(), newRoleTable.getUserId());
        if (existingMember != null) {
            return null;
        }
        RT roleTable = roleService.addRole(newRoleTable);
        membersDao.insert(constructor.create(roleTable.getUserId(), roleTable.getPrincipalId()));
        UserTable userTable = userDAO.get().getUserTable(roleTable.getUserId());
        logJoinedMemberByDefault(roleTable, userTable);
        return roleTable;
    }

    abstract void logJoinedMemberByDefault(RT roleTable, UserTable userTable);

    public void addMember(RT roleTable) {
        membersDao.insert(constructor.create(roleTable.getUserId(), roleTable.getPrincipalId()));
    }

    // TODO user's removing themselves from projects/organizations
    public void removeMember(RT roleTable, String userName, boolean removeRole) {
        membersDao.delete(roleTable.getPrincipalId(), roleTable.getUserId());
        if (removeRole) {
            roleService.deleteRole(roleTable);
        }
        String sb = "Removed:" + userName + " (" + roleTable.getRole().getTitle() + ")";
        logMemberRemoval(roleTable.getPrincipalId(), sb);
    }

    @Transactional
    public void removeMembers(List<HangarApiException> errors, List<Member<R>> members, J joinable) {
        List<RT> toBeRemoved = new ArrayList<>();
        StringBuilder sb = new StringBuilder("Removed: ");
        handleEditOrRemoval(errors, toBeRemoved, members, joinable.getId(), (member, rt, notLast) -> {
            sb.append(member.getName()).append(" (").append(member.getRole().getTitle()).append(")");
            if (notLast) {
                sb.append(", ");
            }
            return true;
        });
        for (RT rt : toBeRemoved) {
            membersDao.delete(rt.getPrincipalId(), rt.getUserId());
            roleService.deleteRole(rt);
        }
        if (!toBeRemoved.isEmpty()) {
            joinableNotificationService.removedFrom(toBeRemoved, joinable);
            logMemberRemoval(joinable.getId(), sb.toString());
        }
    }

    abstract void logMemberRemoval(long principalId, String logEntry);

    @Transactional
    public void editMembers(List<HangarApiException> errors, List<Member<R>> members, J joinable) {
        List<RT> toBeUpdated = new ArrayList<>();
        StringBuilder oldState = new StringBuilder("Old Roles: ");
        StringBuilder newState = new StringBuilder("New Roles: ");
        handleEditOrRemoval(errors, toBeUpdated, members, joinable.getId(), (member, rt, notLast) -> {
            if (member.getRole() == rt.getRole()) {
                return false;
            }
            oldState.append(member.getName()).append(" (").append(rt.getRole().getTitle()).append(")");
            if (notLast) {
                oldState.append(", ");
            }
            rt.setRole(member.getRole());
            newState.append(member.getName()).append(" (").append(rt.getRole().getTitle()).append(")");
            if (notLast) {
                newState.append(", ");
            }
            return true;
        });
        for (RT rt : toBeUpdated) {
            roleService.updateRoles(toBeUpdated);
        }
        if (!toBeUpdated.isEmpty()) {
            joinableNotificationService.roleChanged(toBeUpdated, joinable);
            logMemberUpdate(joinable.getId(), oldState.toString(), newState.toString());
        }
    }

    abstract void logMemberUpdate(long principalId, String oldState, String newState);

    @FunctionalInterface
    private interface AdditionalValidation<R extends Role<RT>, RT extends ExtendedRoleTable<R>> {

        boolean check(Member<R> member, RT rt, boolean notLast);
    }

    private void handleEditOrRemoval(List<HangarApiException> errors, List<RT> toBeChanged, List<Member<R>> members, long principalId, AdditionalValidation<R, RT> consumer) {
        for (int i = 0; i < members.size(); i++) {
            Member<R> member = members.get(i);
            UserTable userTable = userDAO.get().getUserTable(member.getName());
            if (userTable == null) {
                errors.add(new HangarApiException(this.errorPrefix + "invalidUser", member.getName()));
                continue;
            }
            RT roleTable = roleService.getRole(principalId, userTable.getId());
            if (roleTable == null) {
                errors.add(new HangarApiException(this.errorPrefix + "notMember", member.getName()));
                continue;
            }
            if (invalidRolesToChange().contains(member.getRole())) {
                errors.add(new HangarApiException(this.errorPrefix + "invalidRole", member.getRole().getTitle()));
                continue;
            }
            if (invalidRolesToChange().contains(roleTable.getRole())) {
                errors.add(new HangarApiException(this.errorPrefix + "invalidRole", roleTable.getRole().getTitle()));
                continue;
            }
            if (!consumer.check(member, roleTable, i + 1 != members.size())) {
                continue;
            }
            toBeChanged.add(roleTable);
        }
    }

    abstract List<R> invalidRolesToChange();
}
