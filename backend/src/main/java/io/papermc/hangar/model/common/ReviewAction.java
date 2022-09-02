package io.papermc.hangar.model.common;

public enum ReviewAction {
    START,
    MESSAGE,
    STOP,
    REOPEN,
    APPROVE,
    PARTIALLY_APPROVE,
    UNDO_APPROVAL;

    public boolean isApproval() {
        return this == APPROVE || this == PARTIALLY_APPROVE;
    }
}
