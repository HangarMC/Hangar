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

    public DayStats(final LocalDate day, final long reviews, final long uploads, final long totalDownloads, final long unsafeDownloads, final long flagsOpened, final long flagsClosed) {
        this.day = day;
        this.reviews = reviews;
        this.uploads = uploads;
        this.totalDownloads = totalDownloads;
        this.unsafeDownloads = unsafeDownloads;
        this.flagsOpened = flagsOpened;
        this.flagsClosed = flagsClosed;
    }

    public LocalDate getDay() {
        return this.day;
    }

    public long getReviews() {
        return this.reviews;
    }

    public long getUploads() {
        return this.uploads;
    }

    public long getTotalDownloads() {
        return this.totalDownloads;
    }

    public long getUnsafeDownloads() {
        return this.unsafeDownloads;
    }

    public long getFlagsOpened() {
        return this.flagsOpened;
    }

    public long getFlagsClosed() {
        return this.flagsClosed;
    }

    @Override
    public String toString() {
        return "DayStats{" +
                "day=" + this.day +
                ", reviews=" + this.reviews +
                ", uploads=" + this.uploads +
                ", totalDownloads=" + this.totalDownloads +
                ", unsafeDownloads=" + this.unsafeDownloads +
                ", flagsOpened=" + this.flagsOpened +
                ", flagsClosed=" + this.flagsClosed +
                '}';
    }
}
