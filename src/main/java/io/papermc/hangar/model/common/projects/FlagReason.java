package io.papermc.hangar.model.common.projects;

public enum FlagReason {

    INAPPROPRIATE_CONTENT(0, "Inappropriate Content"),
    IMPERSONATION(1, "Impersonation or Deception"),
    SPAM(2, "Spam"),
    MAL_INTENT(3, "Malicious Intent"),
    OTHER(4, "Other");

    private final int value;
    private final String title;

    FlagReason(int value, String title) {
        this.value = value;
        this.title = title;
    }

    public int getValue() {
        return value;
    }

    public String getTitle() {
        return title;
    }

    private static final FlagReason[] VALUES = values();

    public static FlagReason[] getValues() {
        return VALUES;
    }
}
