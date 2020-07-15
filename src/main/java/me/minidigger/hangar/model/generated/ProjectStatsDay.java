package me.minidigger.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * ModelsProtocolsAPIV2ProjectStatsDay
 */
@Validated
public class ProjectStatsDay {
    @JsonProperty("downloads")
    private Long downloads = null;

    @JsonProperty("views")
    private Long views = null;

    public ProjectStatsDay downloads(Long downloads) {
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

    public ProjectStatsDay views(Long views) {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProjectStatsDay projectStatsDay = (ProjectStatsDay) o;
        return Objects.equals(this.downloads, projectStatsDay.downloads) &&
               Objects.equals(this.views, projectStatsDay.views);
    }

    @Override
    public int hashCode() {
        return Objects.hash(downloads, views);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelsProtocolsAPIV2ProjectStatsDay {\n");

        sb.append("    downloads: ").append(toIndentedString(downloads)).append("\n");
        sb.append("    views: ").append(toIndentedString(views)).append("\n");
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
