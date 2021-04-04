package io.papermc.hangar.model.internal.admin;

import java.time.LocalDate;

public class DayStats {

    private final LocalDate day;
    private final long reviews;
    private final long uploads;
    private final long totalDownloads;
    private final long unsafeDownloads;
    private final long flagsOpened;
    private final long flagsClosed;

    public DayStats(LocalDate day, long reviews, long uploads, long totalDownloads, long unsafeDownloads, long flagsOpened, long flagsClosed) {
        this.day = day;
        this.reviews = reviews;
        this.uploads = uploads;
        this.totalDownloads = totalDownloads;
        this.unsafeDownloads = unsafeDownloads;
        this.flagsOpened = flagsOpened;
        this.flagsClosed = flagsClosed;
    }

    public LocalDate getDay() {
        return day;
    }

    public long getReviews() {
        return reviews;
    }

    public long getUploads() {
        return uploads;
    }

    public long getTotalDownloads() {
        return totalDownloads;
    }

    public long getUnsafeDownloads() {
        return unsafeDownloads;
    }

    public long getFlagsOpened() {
        return flagsOpened;
    }

    public long getFlagsClosed() {
        return flagsClosed;
    }

    @Override
    public String toString() {
        return "DayStats{" +
                "day=" + day +
                ", reviews=" + reviews +
                ", uploads=" + uploads +
                ", totalDownloads=" + totalDownloads +
                ", unsafeDownloads=" + unsafeDownloads +
                ", flagsOpened=" + flagsOpened +
                ", flagsClosed=" + flagsClosed +
                '}';
    }
}
