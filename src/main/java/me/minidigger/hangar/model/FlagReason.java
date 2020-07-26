package me.minidigger.hangar.model;

public enum FlagReason {

    INAPPROPRIATECONTENT(0, "Inappropriate Content"),
    IMPERSONATION(1, "Impersonation or Deception"),
    SPAM(2, "Spam"),
    MALINTENT(3, "Malicious Intent"),
    OTHER(4, "Other");

    private int value;
    private String title;

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
}
