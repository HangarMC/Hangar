package io.papermc.hangar.modelold.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * ModelsProtocolsAPIV2ProjectStatsAll
 */
@Validated
public class ProjectStatsAll {
    @JsonProperty("views")
    private Long views = null;

    @JsonProperty("downloads")
    private Long downloads = null;

    @JsonProperty("recent_views")
    private Long recentViews = null;

    @JsonProperty("recent_downloads")
    private Long recentDownloads = null;

    @JsonProperty("stars")
    private Long stars = null;

    @JsonProperty("watchers")
    private Long watchers = null;

    public ProjectStatsAll() {
        //
    }

    public ProjectStatsAll(Long views, Long downloads, Long recentViews, Long recentDownloads, Long stars, Long watchers) {
        this.views = views;
        this.downloads = downloads;
        this.recentViews = recentViews;
        this.recentDownloads = recentDownloads;
        this.stars = stars;
        this.watchers = watchers;
    }

    public Long getViews() {
        return views;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public Long getDownloads() {
        return downloads;
    }

    public void setDownloads(Long downloads) {
        this.downloads = downloads;
    }

    public Long getRecentViews() {
        return recentViews;
    }

    public void setRecentViews(Long recentViews) {
        this.recentViews = recentViews;
    }

    public Long getRecentDownloads() {
        return recentDownloads;
    }

    public void setRecentDownloads(Long recentDownloads) {
        this.recentDownloads = recentDownloads;
    }

   public Long getStars() {
        return stars;
    }

    public void setStars(Long stars) {
        this.stars = stars;
    }

    public Long getWatchers() {
        return watchers;
    }

    public void setWatchers(Long watchers) {
        this.watchers = watchers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectStatsAll that = (ProjectStatsAll) o;

        if (!Objects.equals(views, that.views)) return false;
        if (!Objects.equals(downloads, that.downloads)) return false;
        if (!Objects.equals(recentViews, that.recentViews)) return false;
        if (!Objects.equals(recentDownloads, that.recentDownloads)) return false;
        if (!Objects.equals(stars, that.stars)) return false;
        return Objects.equals(watchers, that.watchers);
    }

    @Override
    public int hashCode() {
        int result = views != null ? views.hashCode() : 0;
        result = 31 * result + (downloads != null ? downloads.hashCode() : 0);
        result = 31 * result + (recentViews != null ? recentViews.hashCode() : 0);
        result = 31 * result + (recentDownloads != null ? recentDownloads.hashCode() : 0);
        result = 31 * result + (stars != null ? stars.hashCode() : 0);
        result = 31 * result + (watchers != null ? watchers.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ProjectStatsAll.class.getSimpleName() + "[", "]")
                .add("views=" + views)
                .add("downloads=" + downloads)
                .add("recentViews=" + recentViews)
                .add("recentDownloads=" + recentDownloads)
                .add("stars=" + stars)
                .add("watchers=" + watchers)
                .toString();
    }
}
