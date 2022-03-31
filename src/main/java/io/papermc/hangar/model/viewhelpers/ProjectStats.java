package io.papermc.hangar.model.viewhelpers;

public class ProjectStats {

    private long views;
    private long downloads;
    private long recentView;
    private long recentDownloads;
    private long stars;
    private long watchers;

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public long getDownloads() {
        return downloads;
    }

    public void setDownloads(long downloads) {
        this.downloads = downloads;
    }

    public long getRecentView() {
        return recentView;
    }

    public void setRecentView(long recentView) {
        this.recentView = recentView;
    }

    public long getRecentDownloads() {
        return recentDownloads;
    }

    public void setRecentDownloads(long recentDownloads) {
        this.recentDownloads = recentDownloads;
    }

    public long getStars() {
        return stars;
    }

    public void setStars(long stars) {
        this.stars = stars;
    }

    public long getWatchers() {
        return watchers;
    }

    public void setWatchers(long watchers) {
        this.watchers = watchers;
    }
}
