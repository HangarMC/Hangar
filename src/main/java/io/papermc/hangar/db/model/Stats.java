package io.papermc.hangar.db.model;

import java.time.LocalDate;

public class Stats {
    private Long review;
    private Long uploads;
    private Long totalDownloads;
    private Long unsafeDownloads;
    private Long flagsOpened;
    private Long flagsClosed;
    private LocalDate day;

    public Long getReview() {
        return review;
    }

    public void setReview(Long review) {
        this.review = review;
    }

    public Long getUploads() {
        return uploads;
    }

    public void setUploads(Long uploads) {
        this.uploads = uploads;
    }

    public Long getTotalDownloads() {
        return totalDownloads;
    }

    public void setTotalDownloads(Long totalDownloads) {
        this.totalDownloads = totalDownloads;
    }

    public Long getUnsafeDownloads() {
        return unsafeDownloads;
    }

    public void setUnsafeDownloads(Long unsafeDownloads) {
        this.unsafeDownloads = unsafeDownloads;
    }

    public Long getFlagsOpened() {
        return flagsOpened;
    }

    public void setFlagsOpened(Long flagsOpened) {
        this.flagsOpened = flagsOpened;
    }

    public Long getFlagsClosed() {
        return flagsClosed;
    }

    public void setFlagsClosed(Long flagsClosed) {
        this.flagsClosed = flagsClosed;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }
}
