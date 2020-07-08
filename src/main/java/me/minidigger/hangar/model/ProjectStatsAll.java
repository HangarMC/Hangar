package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

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

    public ProjectStatsAll views(Long views) {
        this.views = views;
        return this;
    }

    /**
     * Get views
     *
     * @return views
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public Long getViews() {
        return views;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public ProjectStatsAll downloads(Long downloads) {
        this.downloads = downloads;
        return this;
    }

    /**
     * Get downloads
     *
     * @return downloads
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public Long getDownloads() {
        return downloads;
    }

    public void setDownloads(Long downloads) {
        this.downloads = downloads;
    }

    public ProjectStatsAll recentViews(Long recentViews) {
        this.recentViews = recentViews;
        return this;
    }

    /**
     * Get recentViews
     *
     * @return recentViews
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public Long getRecentViews() {
        return recentViews;
    }

    public void setRecentViews(Long recentViews) {
        this.recentViews = recentViews;
    }

    public ProjectStatsAll recentDownloads(Long recentDownloads) {
        this.recentDownloads = recentDownloads;
        return this;
    }

    /**
     * Get recentDownloads
     *
     * @return recentDownloads
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public Long getRecentDownloads() {
        return recentDownloads;
    }

    public void setRecentDownloads(Long recentDownloads) {
        this.recentDownloads = recentDownloads;
    }

    public ProjectStatsAll stars(Long stars) {
        this.stars = stars;
        return this;
    }

    /**
     * Get stars
     *
     * @return stars
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public Long getStars() {
        return stars;
    }

    public void setStars(Long stars) {
        this.stars = stars;
    }

    public ProjectStatsAll watchers(Long watchers) {
        this.watchers = watchers;
        return this;
    }

    /**
     * Get watchers
     *
     * @return watchers
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public Long getWatchers() {
        return watchers;
    }

    public void setWatchers(Long watchers) {
        this.watchers = watchers;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProjectStatsAll projectStatsAll = (ProjectStatsAll) o;
        return Objects.equals(this.views, projectStatsAll.views) &&
               Objects.equals(this.downloads, projectStatsAll.downloads) &&
               Objects.equals(this.recentViews, projectStatsAll.recentViews) &&
               Objects.equals(this.recentDownloads, projectStatsAll.recentDownloads) &&
               Objects.equals(this.stars, projectStatsAll.stars) &&
               Objects.equals(this.watchers, projectStatsAll.watchers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(views, downloads, recentViews, recentDownloads, stars, watchers);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelsProtocolsAPIV2ProjectStatsAll {\n");

        sb.append("    views: ").append(toIndentedString(views)).append("\n");
        sb.append("    downloads: ").append(toIndentedString(downloads)).append("\n");
        sb.append("    recentViews: ").append(toIndentedString(recentViews)).append("\n");
        sb.append("    recentDownloads: ").append(toIndentedString(recentDownloads)).append("\n");
        sb.append("    stars: ").append(toIndentedString(stars)).append("\n");
        sb.append("    watchers: ").append(toIndentedString(watchers)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
