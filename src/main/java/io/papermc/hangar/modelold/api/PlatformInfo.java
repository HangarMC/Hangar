package io.papermc.hangar.modelold.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.modelold.Platform.PlatformCategory;
import io.papermc.hangar.modelold.generated.TagColor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;


@Validated
@ApiModel(value = "PlatformInfo", description = "Information on a supported platform")
public class PlatformInfo {

    @JsonProperty("name")
    private final String name;
    @JsonProperty("url")
    private final String url;
    @JsonProperty("category")
    private final PlatformCategory category;
    @JsonProperty("possibleVersions")
    private final List<String> possibleVersions;
    @JsonProperty("tag")
    private final TagColor tagColor;

    public PlatformInfo(@NotNull String name, String url, @NotNull PlatformCategory category, @NotNull List<String> possibleVersions, @NotNull TagColor tagColor) {
        this.name = name;
        this.url = url;
        this.category = category;
        this.possibleVersions = possibleVersions;
        this.tagColor = tagColor;
    }

    @NotNull
    @ApiModelProperty("Platform name")
    public String getName() {
        return name;
    }

    @NotNull
    @ApiModelProperty("Platform url")
    public String getUrl() {
        return url;
    }

    @NotNull
    @ApiModelProperty("Platform category")
    public PlatformCategory getCategory() {
        return category;
    }

    @NotNull
    @ApiModelProperty("Valid versions for this platform")
    public List<String> getPossibleVersions() {
        return possibleVersions;
    }

    @NotNull
    @ApiModelProperty("Tag color for this platform")
    public TagColor getTagColor() {
        return tagColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlatformInfo that = (PlatformInfo) o;
        return name.equals(that.name) &&
                url.equals(that.url) &&
//                category == that.category &&
                possibleVersions.equals(that.possibleVersions) &&
                tagColor.equals(that.tagColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, /*category,*/ possibleVersions, tagColor);
    }

    @Override
    public String toString() {
        return "PlatformInfo{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
//                ", category=" + category +
                ", possibleVersions=" + possibleVersions +
                ", tagColor=" + tagColor +
                '}';
    }
}
