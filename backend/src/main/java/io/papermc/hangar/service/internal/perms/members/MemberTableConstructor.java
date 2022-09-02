package io.papermc.hangar.service.internal.perms.members;

import io.papermc.hangar.model.db.members.MemberTable;

@FunctionalInterface
public interface MemberTableConstructor<MT extends MemberTable> {

    MT create(long userId, long principalId);
}
