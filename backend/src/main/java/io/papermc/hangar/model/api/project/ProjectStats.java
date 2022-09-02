package io.papermc.hangar.model.api.project;

public class ProjectStats {

    private final long views;
    private final long downloads;
    private final long recentViews;
    private final long recentDownloads;
    private final long stars;
    private final long watchers;

    public ProjectStats(long views, long downloads, long recentViews, long recentDownloads, long stars, long watchers) {
        this.views = views;
        this.downloads = downloads;
        this.recentViews = recentViews;
        this.recentDownloads = recentDownloads;
        this.stars = stars;
        this.watchers = watchers;
    }

    public long getViews() {
        return views;
    }

    public long getDownloads() {
        return downloads;
    }

    public long getRecentViews() {
        return recentViews;
    }

    public long getRecentDownloads() {
        return recentDownloads;
    }

    public long getStars() {
        return stars;
    }

    public long getWatchers() {
        return watchers;
    }

    @Override
    public String toString() {
        return "ProjectStats{" +
                "views=" + views +
                ", downloads=" + downloads +
                ", recentViews=" + recentViews +
                ", recentDownloads=" + recentDownloads +
                ", stars=" + stars +
                ", watchers=" + watchers +
                '}';
    }
}
