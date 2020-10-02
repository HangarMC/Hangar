package io.papermc.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.model.Platform;
import io.swagger.annotations.ApiModelProperty;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

@Validated
public class PlatformDependency {

    @JsonProperty("name")
    @JsonFormat(shape = Shape.STRING)
    private Platform platform;

    @JsonProperty("versions")
    private List<String> versions;

    public PlatformDependency(Platform platform, List<String> versions) {
        this.platform = platform;
        this.versions = versions;
    }

    public PlatformDependency() { }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlatformDependency that = (PlatformDependency) o;
        return platform == that.platform &&
                versions.equals(that.versions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, versions);
    }

    @Override
    public String toString() {
        return "PlatformDependency{" +
                "platform=" + platform +
                ", versions=" + versions +
                '}';
    }
}
