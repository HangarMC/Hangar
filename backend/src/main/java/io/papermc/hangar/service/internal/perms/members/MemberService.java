package io.papermc.hangar.service.internal.perms.members;

import io.papermc.hangar.HangarComponent;
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
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.LogContext;
import io.papermc.hangar.model.loggable.Loggable;
import io.papermc.hangar.service.internal.perms.roles.RoleService;
import io.papermc.hangar.service.internal.users.notifications.JoinableNotificationService;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Transactional
public abstract class MemberService<
    LC extends LogContext<?, LC>,
    R extends Role<RT>,
    RT extends ExtendedRoleTable<R, LC>,
    RD extends IRolesDAO<RT>,
    S extends RoleService<RT, R, RD>,
    J extends Table & Named & Loggable<LC>,
    JNS extends JoinableNotificationService<RT, J>,
    MD extends MembersDAO<MT>,
    MT extends MemberTable
    > extends HangarComponent {

    @Autowired
    private UserDAO userDAO;

    private final S roleService;
    private final MD membersDao;
    private final JNS joinableNotificationService;
    private final MemberTableConstructor<MT> constructor;
    private final String errorPrefix;

    private final LogAction<LC> memberAddedAction;
    private final LogAction<LC> membersRemovedAction;
    private final LogAction<LC> memberRoleChangedAction;

    protected MemberService(S roleService, MD membersDao, JNS joinableNotificationService, MemberTableConstructor<MT> constructor, String errorPrefix, LogAction<LC> memberAddedAction, LogAction<LC> membersRemovedAction, LogAction<LC> memberRoleChangedAction) {
        this.roleService = roleService;
        this.membersDao = membersDao;
        this.joinableNotificationService = joinableNotificationService;
        this.constructor = constructor;
        this.errorPrefix = errorPrefix;
        this.memberAddedAction = memberAddedAction;
        this.membersRemovedAction = membersRemovedAction;
        this.memberRoleChangedAction = memberRoleChangedAction;
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
        UserTable userTable = userDAO.getUserTable(roleTable.getUserId());
        roleTable.logAction(actionLogger, memberAddedAction, userTable.getName() + " joined due to creation", "");
        return roleTable;
    }

    public void addMember(RT roleTable) {
        membersDao.insert(constructor.create(roleTable.getUserId(), roleTable.getPrincipalId()));
    }

    @Transactional
    public void leave(final J joinable) {
        final RT role = roleService.getRole(joinable.getId(), getHangarUserId());
        if (invalidRolesToChange().contains(role.getRole())) {
            throw new HangarApiException(this.errorPrefix + "invalidRole", role.getRole().getTitle());
        }

        membersDao.delete(role.getPrincipalId(), role.getUserId());
        roleService.deleteRole(role);
        logMemberRemoval(role, "Left:" + getHangarPrincipal().getName() + " (" + role.getRole().getTitle() + ")");
    }

    @Transactional
    public void removeMember(Member<R> member, J joinable) {
        RT roleTable = handleEditOrRemoval(member, joinable.getId());
        membersDao.delete(roleTable.getPrincipalId(), roleTable.getUserId());
        roleService.deleteRole(roleTable);
        joinableNotificationService.removedFrom(roleTable, joinable);
        logMemberRemoval(joinable, "Removed: " + member.getName() + " (" + member.getRole().getTitle() + ")");
    }

    private void logMemberRemoval(Loggable<LC> loggable, String logEntry) {
        loggable.logAction(actionLogger, membersRemovedAction, logEntry, "");
    }

    @Transactional
    public void editMember(Member<R> member, J joinable) {
        RT roleTable = handleEditOrRemoval(member, joinable.getId());
        if (member.getRole() == roleTable.getRole()) {
            return;
        }

        String oldTitle = roleTable.getRole().getTitle();
        roleTable.setRole(member.getRole());

        roleService.updateRole(roleTable);
        joinableNotificationService.roleChanged(roleTable, joinable);
        logMemberUpdate(joinable,
            "Old Roles: " + member.getName() + " (" + oldTitle + ")",
            "New Roles: " + member.getName() + " (" + roleTable.getRole().getTitle() + ")");
    }

    private void logMemberUpdate(Loggable<LC> loggable, String oldState, String newState) {
        loggable.logAction(actionLogger, memberRoleChangedAction, newState, oldState);
    }

    private RT handleEditOrRemoval(Member<R> member, long principalId) {
        UserTable userTable = userDAO.getUserTable(member.getName());
        if (userTable == null) {
            throw new HangarApiException(this.errorPrefix + "invalidUser", member.getName());
        }

        RT roleTable = roleService.getRole(principalId, userTable.getId());
        if (roleTable == null) {
            throw new HangarApiException(this.errorPrefix + "notMember", member.getName());
        }
        if (invalidRolesToChange().contains(member.getRole())) {
            throw new HangarApiException(this.errorPrefix + "invalidRole", member.getRole().getTitle());
        }
        if (invalidRolesToChange().contains(roleTable.getRole())) {
            throw new HangarApiException(this.errorPrefix + "invalidRole", roleTable.getRole().getTitle());
        }
        return roleTable;
    }

    abstract List<R> invalidRolesToChange();
}
