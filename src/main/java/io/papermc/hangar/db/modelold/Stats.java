package io.papermc.hangar.db.modelold;

import java.time.LocalDate;

public class Stats {

    private long reviews;
    private long uploads;
    private long totalDownloads;
    private long unsafeDownloads;
    private long flagsOpened;
    private long flagsClosed;
    private LocalDate day;

    public long getReviews() {
        return reviews;
    }

    public void setReviews(long review) {
        this.reviews = review;
    }

    public long getUploads() {
        return uploads;
    }

    public void setUploads(long uploads) {
        this.uploads = uploads;
    }

    public long getTotalDownloads() {
        return totalDownloads;
    }

    public void setTotalDownloads(long totalDownloads) {
        this.totalDownloads = totalDownloads;
    }

    public long getUnsafeDownloads() {
        return unsafeDownloads;
    }

    public void setUnsafeDownloads(long unsafeDownloads) {
        this.unsafeDownloads = unsafeDownloads;
    }

    public long getFlagsOpened() {
        return flagsOpened;
    }

    public void setFlagsOpened(long flagsOpened) {
        this.flagsOpened = flagsOpened;
    }

    public long getFlagsClosed() {
        return flagsClosed;
    }

    public void setFlagsClosed(long flagsClosed) {
        this.flagsClosed = flagsClosed;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }
}
