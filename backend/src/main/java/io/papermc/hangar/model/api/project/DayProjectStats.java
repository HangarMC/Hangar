package io.papermc.hangar.model.api.project;

public class DayProjectStats {

    private final long views;
    private final long downloads;

    public DayProjectStats(long views, long downloads) {
        this.views = views;
        this.downloads = downloads;
    }

    public long getViews() {
        return views;
    }

    public long getDownloads() {
        return downloads;
    }

    @Override
    public String toString() {
        return "DayProjectStats{" +
                "views=" + views +
                ", downloads=" + downloads +
                '}';
    }
}
