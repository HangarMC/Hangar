package io.papermc.hangar.model.common.projects;

public enum FlagReason {

    INAPPROPRIATE_CONTENT("Inappropriate Content"),
    IMPERSONATION("Impersonation or Deception"),
    SPAM("Spam"),
    MAL_INTENT("Malicious Intent"),
    OTHER("Other");

    private final String title;

    FlagReason(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    private static final FlagReason[] VALUES = values();

    public static FlagReason[] getValues() {
        return VALUES;
    }
}
