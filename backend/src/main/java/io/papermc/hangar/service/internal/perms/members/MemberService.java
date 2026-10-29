package io.papermc.hangar.service.internal.perms.members;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.OrganizationDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.members.MembersDAO;
import io.papermc.hangar.db.dao.internal.table.roles.IMemberRolesDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.MemberPermissions;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.OrganizationTable;
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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class MemberService<
    LC extends LogContext<?, LC>,
    RT extends ExtendedRoleTable<LC>,
    RD extends IMemberRolesDAO<RT>,
    S extends RoleService<RT, RD>,
    J extends Table & Named & Loggable<LC>,
    JNS extends JoinableNotificationService<RT, J>,
    MD extends MembersDAO<MT>,
    MT extends MemberTable
    > extends HangarComponent {

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private OrganizationDAO organizationDAO;

    private final S roleService;
    private final MD membersDao;
    private final JNS joinableNotificationService;
    private final MemberTableConstructor<MT> constructor;
    private final String errorPrefix;
    private final boolean organization;

    private final LogAction<LC> memberAddedAction;
    private final LogAction<LC> membersRemovedAction;
    private final LogAction<LC> memberRoleChangedAction;

    protected MemberService(final S roleService, final MD membersDao, final JNS joinableNotificationService, final MemberTableConstructor<MT> constructor, final String errorPrefix, final boolean organization, final LogAction<LC> memberAddedAction, final LogAction<LC> membersRemovedAction, final LogAction<LC> memberRoleChangedAction) {
        this.roleService = roleService;
        this.membersDao = membersDao;
        this.joinableNotificationService = joinableNotificationService;
        this.constructor = constructor;
        this.errorPrefix = errorPrefix;
        this.organization = organization;
        this.memberAddedAction = memberAddedAction;
        this.membersRemovedAction = membersRemovedAction;
        this.memberRoleChangedAction = memberRoleChangedAction;
    }

    // extra bits are dropped rather than rejected, so a stale client cannot widen a membership past the editor
    public Permission sanitize(final Permission requested) {
        return requested.intersect(MemberPermissions.assignable(this.organization)).add(MemberPermissions.base(this.organization));
    }

    public Permission ownerPermissions() {
        return MemberPermissions.owner(this.organization);
    }

    public String requireTitle(final EditMembersForm.Member member) {
        final String title = member.getTitle() == null ? null : member.getTitle().trim();
        if (title == null || title.isEmpty()) {
            throw new HangarApiException(this.errorPrefix + "missingTitle", member.getName());
        }
        return title;
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

    public void addMemberIfNeeded(final RT roleTable) {
        final MT existingMember = this.membersDao.getMemberTable(roleTable.getPrincipalId(), roleTable.getUserId());
        if (existingMember == null) {
            this.membersDao.insert(this.constructor.create(roleTable.getUserId(), roleTable.getPrincipalId()));
        }
    }

    @Transactional
    public void leave(final J joinable) {
        final RT role = this.roleService.getRole(joinable.getId(), this.getHangarUserId());
        if (role.isOwner()) {
            throw new HangarApiException(this.errorPrefix + "ownerCannotLeave");
        }

        this.membersDao.delete(role.getPrincipalId(), role.getUserId());
        this.roleService.deleteRole(role);
        this.logMemberRemoval(role, "Left:" + this.getHangarPrincipal().getName() + " (" + role.getTitle() + ")");
    }

    @Transactional
    public void removeMember(final EditMembersForm.Member member, final J joinable) {
        final RT roleTable = this.getEditableRole(member, joinable.getId());
        if (roleTable.isOwner()) {
            // a pending owner row is a transfer request, cancelled through its own endpoint
            throw new HangarApiException(this.errorPrefix + "cannotRemoveOwner", member.getName());
        }
        this.membersDao.delete(roleTable.getPrincipalId(), roleTable.getUserId());
        this.roleService.deleteRole(roleTable);

        final OrganizationTable org = this.organizationDAO.getByUserId(roleTable.getUserId());
        // notify org owner
        if (org != null) {
            this.joinableNotificationService.removedFromOrg(roleTable, org, joinable, this.getHangarUserId());
        } else {
            this.joinableNotificationService.removedFrom(roleTable, joinable, this.getHangarUserId());
        }

        this.logMemberRemoval(joinable, "Removed: " + member.getName() + " (" + roleTable.getTitle() + ")");
    }

    private void logMemberRemoval(final Loggable<LC> loggable, final String logEntry) {
        loggable.logAction(this.actionLogger, this.membersRemovedAction, logEntry, "");
    }

    @Transactional
    public void editMember(final EditMembersForm.Member member, final J joinable) {
        final RT roleTable = this.getEditableRole(member, joinable.getId());
        final String title = this.requireTitle(member);
        final Permission permissions = roleTable.isOwner() ? roleTable.getPermissions() : this.sanitize(member.asPermission());
        if (title.equals(roleTable.getTitle()) && permissions.equals(roleTable.getPermissions())) {
            return;
        }

        final String oldState = roleTable.getTitle() + " " + roleTable.getPermissions().toNamed();
        roleTable.setTitle(title);
        roleTable.setPermissions(permissions);

        this.roleService.updateRole(roleTable);

        this.logMemberUpdate(joinable,
            "Old: " + member.getName() + " (" + oldState + ")",
            "New: " + member.getName() + " (" + title + " " + permissions.toNamed() + ")");
    }

    private void logMemberUpdate(final Loggable<LC> loggable, final String oldState, final String newState) {
        loggable.logAction(this.actionLogger, this.memberRoleChangedAction, newState, oldState);
    }

    private RT getEditableRole(final EditMembersForm.Member member, final long principalId) {
        final UserTable userTable = this.userDAO.getUserTable(member.getName());
        if (userTable == null) {
            throw new HangarApiException(this.errorPrefix + "invalidUser", member.getName());
        }

        final RT roleTable = this.roleService.getRole(principalId, userTable.getId());
        if (roleTable == null) {
            throw new HangarApiException(this.errorPrefix + "notMember", member.getName());
        }
        return roleTable;
    }
}
