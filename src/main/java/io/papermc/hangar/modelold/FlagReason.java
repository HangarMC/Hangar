package io.papermc.hangar.modelold;

public enum FlagReason {

    INAPPROPRIATECONTENT(0, "Inappropriate Content"),
    IMPERSONATION(1, "Impersonation or Deception"),
    SPAM(2, "Spam"),
    MALINTENT(3, "Malicious Intent"),
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
