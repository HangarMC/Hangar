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
import io.papermc.hangar.model.internal.api.requests.EditMembersForm;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.LogContext;
import io.papermc.hangar.model.loggable.Loggable;
import io.papermc.hangar.service.internal.perms.roles.RoleService;
import io.papermc.hangar.service.internal.users.notifications.JoinableNotificationService;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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

    protected MemberService(final S roleService, final MD membersDao, final JNS joinableNotificationService, final MemberTableConstructor<MT> constructor, final String errorPrefix, final LogAction<LC> memberAddedAction, final LogAction<LC> membersRemovedAction, final LogAction<LC> memberRoleChangedAction) {
        this.roleService = roleService;
        this.membersDao = membersDao;
        this.joinableNotificationService = joinableNotificationService;
        this.constructor = constructor;
        this.errorPrefix = errorPrefix;
        this.memberAddedAction = memberAddedAction;
        this.membersRemovedAction = membersRemovedAction;
        this.memberRoleChangedAction = memberRoleChangedAction;
    }

    public @Nullable RT addNewAcceptedByDefaultMember(final RT newRoleTable) {
        if (!newRoleTable.isAccepted()) {
            throw new IllegalArgumentException("Should only be used by accepted roles");
        }
        final MT existingMember = this.membersDao.getMemberTable(newRoleTable.getPrincipalId(), newRoleTable.getUserId());
        if (existingMember != null) {
            return null;
        }
        final RT roleTable = this.roleService.addRole(newRoleTable);
        this.membersDao.insert(this.constructor.create(roleTable.getUserId(), roleTable.getPrincipalId()));
        final UserTable userTable = this.userDAO.getUserTable(roleTable.getUserId());
        roleTable.logAction(this.actionLogger, this.memberAddedAction, userTable.getName() + " joined due to creation", "");
        return roleTable;
    }

    public void addMember(final RT roleTable) {
        this.membersDao.insert(this.constructor.create(roleTable.getUserId(), roleTable.getPrincipalId()));
    }

    @Transactional
    public void leave(final J joinable) {
        final RT role = this.roleService.getRole(joinable.getId(), this.getHangarUserId());
        if (this.invalidRolesToChange().contains(role.getRole())) {
            throw new HangarApiException(this.errorPrefix + "invalidRole", role.getRole().getTitle());
        }

        this.membersDao.delete(role.getPrincipalId(), role.getUserId());
        this.roleService.deleteRole(role);
        this.logMemberRemoval(role, "Left:" + this.getHangarPrincipal().getName() + " (" + role.getRole().getTitle() + ")");
    }

    @Transactional
    public void removeMember(final EditMembersForm.Member<R> member, final J joinable) {
        final RT roleTable = this.handleEditOrRemoval(member, joinable.getId());
        this.membersDao.delete(roleTable.getPrincipalId(), roleTable.getUserId());
        this.roleService.deleteRole(roleTable);
        this.joinableNotificationService.removedFrom(roleTable, joinable, this.getHangarUserId());
        this.logMemberRemoval(joinable, "Removed: " + member.getName() + " (" + member.getRole().getTitle() + ")");
    }

    private void logMemberRemoval(final Loggable<LC> loggable, final String logEntry) {
        loggable.logAction(this.actionLogger, this.membersRemovedAction, logEntry, "");
    }

    @Transactional
    public void editMember(final EditMembersForm.Member<R> member, final J joinable) {
        final RT roleTable = this.handleEditOrRemoval(member, joinable.getId());
        if (member.getRole() == roleTable.getRole()) {
            return;
        }

        final String oldTitle = roleTable.getRole().getTitle();
        roleTable.setRole(member.getRole());

        this.roleService.updateRole(roleTable);
        this.joinableNotificationService.roleChanged(roleTable, joinable, this.getHangarUserId());
        this.logMemberUpdate(joinable,
            "Old Roles: " + member.getName() + " (" + oldTitle + ")",
            "New Roles: " + member.getName() + " (" + roleTable.getRole().getTitle() + ")");
    }

    private void logMemberUpdate(final Loggable<LC> loggable, final String oldState, final String newState) {
        loggable.logAction(this.actionLogger, this.memberRoleChangedAction, newState, oldState);
    }

    private RT handleEditOrRemoval(final EditMembersForm.Member<R> member, final long principalId) {
        final UserTable userTable = this.userDAO.getUserTable(member.getName());
        if (userTable == null) {
            throw new HangarApiException(this.errorPrefix + "invalidUser", member.getName());
        }

        final RT roleTable = this.roleService.getRole(principalId, userTable.getId());
        if (roleTable == null) {
            throw new HangarApiException(this.errorPrefix + "notMember", member.getName());
        }
        if (this.invalidRolesToChange().contains(member.getRole())) {
            throw new HangarApiException(this.errorPrefix + "invalidRole", member.getRole().getTitle());
        }
        if (this.invalidRolesToChange().contains(roleTable.getRole())) {
            throw new HangarApiException(this.errorPrefix + "invalidRole", roleTable.getRole().getTitle());
        }
        return roleTable;
    }

    abstract List<R> invalidRolesToChange();
}
