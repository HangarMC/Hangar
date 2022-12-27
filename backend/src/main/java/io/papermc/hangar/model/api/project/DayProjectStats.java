package io.papermc.hangar.model.api.project;

public class DayProjectStats {

    private final long views;
    private final long downloads;

    public DayProjectStats(final long views, final long downloads) {
        this.views = views;
        this.downloads = downloads;
    }

    public long getViews() {
        return this.views;
    }

    public long getDownloads() {
        return this.downloads;
    }

    @Override
    public String toString() {
        return "DayProjectStats{" +
                "views=" + this.views +
                ", downloads=" + this.downloads +
                '}';
    }
}
