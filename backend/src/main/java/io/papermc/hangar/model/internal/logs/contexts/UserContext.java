package io.papermc.hangar.model.internal.logs.contexts;

import io.papermc.hangar.model.db.log.LoggedActionsUserTable;

public class UserContext extends LogContext<LoggedActionsUserTable, UserContext> {
    
    private final Long userId;

    private UserContext(Long userId) {
        super(Context.USER, LoggedActionsUserTable::new);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public static UserContext of(Long userId) {
        return new UserContext(userId);
    }
}
