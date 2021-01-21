package io.papermc.hangar.modelold.generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.modelold.Platform;
import io.papermc.hangar.util.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

@Validated
public class PlatformDependency {

    @JsonProperty(value = "name", required = true)
    @JsonFormat(shape = Shape.STRING)
    private Platform platform;

    @JsonProperty(value = "versions", required = true)
    private List<String> versions;

    @JsonProperty("formatted_versions")
    private final String formattedVersion;

    public PlatformDependency(Platform platform, List<String> versions) {
        this.platform = platform;
        this.versions = versions;
        this.formattedVersion = PlatformDependency.formatVersions(this.versions);
    }

    @JsonCreator
    public PlatformDependency(@JsonProperty(value = "name", required = true) Platform platform, @JsonProperty(value = "versions", required = true) List<String> versions, @JsonProperty("formatted_versions") String formattedVersion) {
        this.platform = platform;
        this.versions = versions;
        if (formattedVersion == null) {
            this.formattedVersion = PlatformDependency.formatVersions(this.versions);
        } else {
            this.formattedVersion = formattedVersion;
        }
    }

    /**
     * Get the platform for this version
     * @return platform
     */
    @NotNull
    @ApiModelProperty(value = "Platform for this version", required = true)
    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    /**
     * Get valid versions for this platform & version
     * @return list of versions
     */
    @NotNull
    @ApiModelProperty(value = "Valid versions", required = true)
    public List<String> getVersions() {
        return versions;
    }

    public void setVersions(List<String> versions) {
        this.versions = versions;
    }

    public void addVersion(String version) {
        if (!this.versions.contains(version)) {
            this.versions.add(version);
        }
    }

    @NotNull
    @ApiModelProperty(value = "Formatted version string")
    public String getFormattedVersion() {
        return formattedVersion;
    }

    private static String formatVersions(List<String> versions) {
        return StringUtils.formatVersionNumbers(versions);
    }

    @Override
    public String toString() {
        return "PlatformDependency{" +
                "platform=" + platform +
                ", versions=" + versions +
                ", formattedVersion='" + formattedVersion + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlatformDependency that = (PlatformDependency) o;
        return platform == that.platform &&
                versions.equals(that.versions) &&
                formattedVersion.equals(that.formattedVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, versions, formattedVersion);
    }

}
