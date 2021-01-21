package io.papermc.hangar.modelold.viewhelpers;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.db.modelold.RoleTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.modelold.Permission;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class JoinableData<R extends RoleTable, T> {

    protected T joinable;
    protected long ownerId;
    protected String ownerName;
    protected Map<R, UsersTable> members;
    protected RoleCategory roleCategory;

    public JoinableData(T joinable, long ownerId, String ownerName, Map<R, UsersTable> members, RoleCategory roleCategory) {
        this.joinable = joinable;
        this.ownerName = ownerName;
        this.ownerId = ownerId;
        this.members = members;
        this.roleCategory = roleCategory;
    }

    public T getJoinable() {
        return joinable;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Map<R, UsersTable> getMembers() {
        return members;
    }

    public RoleCategory getRoleCategory() {
        return roleCategory;
    }

    public Map<R, UsersTable> filteredMembers(HeaderData headerData) {
        boolean hasEditMembers = headerData.globalPerm(Permission.ManageSubjectMembers);
        boolean userIsOwner = headerData.isAuthenticated() && headerData.getCurrentUser().getId() == ownerId;
        if (hasEditMembers || userIsOwner) {
            return members;
        }
        else return members.entrySet().stream().filter(member -> member.getKey().getIsAccepted()).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}
