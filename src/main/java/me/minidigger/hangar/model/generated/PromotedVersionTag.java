package me.minidigger.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * ModelsProtocolsAPIV2PromotedVersionTag
 */
@Validated
public class PromotedVersionTag {
    @JsonProperty("name")
    private String name = null;

    @JsonProperty("data")
    private String data = null;

    @JsonProperty("display_data")
    private String displayData = null;

    @JsonProperty("minecraft_version")
    private String minecraftVersion = null;

    @JsonProperty("color")
    private TagColor color = null;

    public PromotedVersionTag name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
     *
     * @return name
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PromotedVersionTag data(String data) {
        this.data = data;
        return this;
    }

    /**
     * Get data
     *
     * @return data
     **/
    @ApiModelProperty(value = "")

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public PromotedVersionTag displayData(String displayData) {
        this.displayData = displayData;
        return this;
    }

    /**
     * Get displayData
     *
     * @return displayData
     **/
    @ApiModelProperty(value = "")

    public String getDisplayData() {
        return displayData;
    }

    public void setDisplayData(String displayData) {
        this.displayData = displayData;
    }

    public PromotedVersionTag minecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
        return this;
    }

    /**
     * Get minecraftVersion
     *
     * @return minecraftVersion
     **/
    @ApiModelProperty(value = "")

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public PromotedVersionTag color(TagColor color) {
        this.color = color;
        return this;
    }

    /**
     * Get color
     *
     * @return color
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid
    public TagColor getColor() {
        return color;
    }

    public void setColor(TagColor color) {
        this.color = color;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PromotedVersionTag promotedVersionTag = (PromotedVersionTag) o;
        return Objects.equals(this.name, promotedVersionTag.name) &&
               Objects.equals(this.data, promotedVersionTag.data) &&
               Objects.equals(this.displayData, promotedVersionTag.displayData) &&
               Objects.equals(this.minecraftVersion, promotedVersionTag.minecraftVersion) &&
               Objects.equals(this.color, promotedVersionTag.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, data, displayData, minecraftVersion, color);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelsProtocolsAPIV2PromotedVersionTag {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    data: ").append(toIndentedString(data)).append("\n");
        sb.append("    displayData: ").append(toIndentedString(displayData)).append("\n");
        sb.append("    minecraftVersion: ").append(toIndentedString(minecraftVersion)).append("\n");
        sb.append("    color: ").append(toIndentedString(color)).append("\n");
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
