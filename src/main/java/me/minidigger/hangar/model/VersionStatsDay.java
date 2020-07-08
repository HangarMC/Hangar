package me.minidigger.hangar.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * ModelsProtocolsAPIV2VersionStatsDay
 */
@Validated
public class VersionStatsDay {
    @JsonProperty("downloads")
    private Long downloads = null;

    public VersionStatsDay downloads(Long downloads) {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VersionStatsDay versionStatsDay = (VersionStatsDay) o;
        return Objects.equals(this.downloads, versionStatsDay.downloads);
    }

    @Override
    public int hashCode() {
        return Objects.hash(downloads);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelsProtocolsAPIV2VersionStatsDay {\n");

        sb.append("    downloads: ").append(toIndentedString(downloads)).append("\n");
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
