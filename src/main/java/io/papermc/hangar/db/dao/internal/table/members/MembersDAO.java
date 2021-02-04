package io.papermc.hangar.db.dao.internal.table.members;

import io.papermc.hangar.model.db.members.MemberTable;

public interface MembersDAO<T extends MemberTable> {

    T insert(T table);

    T getMemberTable(long principalId, long userId);

    void delete(long principalId, long userId);
}
