package io.papermc.hangar.model.api.project;

public class ProjectStats {

    private final long views;
    private final long downloads;
    private final long recentViews;
    private final long recentDownloads;
    private final long stars;
    private final long watchers;

    public ProjectStats(final long views, final long downloads, final long recentViews, final long recentDownloads, final long stars, final long watchers) {
        this.views = views;
        this.downloads = downloads;
        this.recentViews = recentViews;
        this.recentDownloads = recentDownloads;
        this.stars = stars;
        this.watchers = watchers;
    }

    public long getViews() {
        return this.views;
    }

    public long getDownloads() {
        return this.downloads;
    }

    public long getRecentViews() {
        return this.recentViews;
    }

    public long getRecentDownloads() {
        return this.recentDownloads;
    }

    public long getStars() {
        return this.stars;
    }

    public long getWatchers() {
        return this.watchers;
    }

    @Override
    public String toString() {
        return "ProjectStats{" +
                "views=" + this.views +
                ", downloads=" + this.downloads +
                ", recentViews=" + this.recentViews +
                ", recentDownloads=" + this.recentDownloads +
                ", stars=" + this.stars +
                ", watchers=" + this.watchers +
                '}';
    }
}
