package io.papermc.hangar.modelold.viewhelpers;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class VersionReviewMessage {

    private String message;
    private long time;
    private String action;

    public VersionReviewMessage(String message) {
        this.message = message;
        this.time = System.currentTimeMillis();
        this.action = "message";
    }

    public VersionReviewMessage(String message, long time, String action) {
        this.message = message;
        this.time = time;
        this.action = action;
    }

    public VersionReviewMessage() { }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @JsonIgnore
    public String getFormattedTime() {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(OffsetDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC));
    }

    @JsonIgnore
    public boolean isTakeover() {
        return action.equalsIgnoreCase("takeover");
    }

    @JsonIgnore
    public boolean isStop() {
        return action.equalsIgnoreCase("stop");
    }
}
