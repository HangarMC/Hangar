package me.minidigger.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * ModelsProtocolsAPIV2VersionTagColor
 */
@Validated
public class TagColor {

    @JsonProperty("foreground")
    private String foreground = null;

    @JsonProperty("background")
    private String background = null;

    public TagColor foreground(String foreground) {
        this.foreground = foreground;
        return this;
    }

    /**
     * Get foreground
     *
     * @return foreground
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getForeground() {
        return foreground;
    }

    public void setForeground(String foreground) {
        this.foreground = foreground;
    }

    public TagColor background(String background) {
        this.background = background;
        return this;
    }

    /**
     * Get background
     *
     * @return background
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TagColor tagColor = (TagColor) o;
        return Objects.equals(this.foreground, tagColor.foreground) &&
               Objects.equals(this.background, tagColor.background);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreground, background);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelsProtocolsAPIV2VersionTagColor {\n");

        sb.append("    foreground: ").append(toIndentedString(foreground)).append("\n");
        sb.append("    background: ").append(toIndentedString(background)).append("\n");
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
