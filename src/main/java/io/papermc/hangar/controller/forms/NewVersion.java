package io.papermc.hangar.controller.forms;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.controller.forms.objects.Channel;
import io.papermc.hangar.model.generated.PlatformDependency;
import io.papermc.hangar.model.viewhelpers.VersionDependencies;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class NewVersion {

    @JsonProperty(value = "channel", required = true)
    private Channel channel;

    @JsonProperty(value = "dependencies", required = true)
    private VersionDependencies versionDependencies;

    @JsonProperty("externalUrl")
    private String externalUrl;

    @JsonProperty(value = "forumSync", required = true)
    private boolean forumSync;

    @JsonProperty(value = "platforms", required = true)
    private List<PlatformDependency> platformDependencies;

    @JsonProperty(value = "recommended", required = true)
    private boolean recommended;

    @JsonProperty(value = "unstable", required = true)
    private boolean unstable;

    @JsonProperty(value = "versionString", required = true)
    private String versionString;

    @JsonProperty(value = "content", required = true)
    private String content;

    @NotNull
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @NotNull
    public VersionDependencies getVersionDependencies() {
        return versionDependencies;
    }

    public void setVersionDependencies(VersionDependencies versionDependencies) {
        this.versionDependencies = versionDependencies;
    }

    @Nullable
    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public boolean isForumSync() {
        return forumSync;
    }

    public void setForumSync(boolean forumSync) {
        this.forumSync = forumSync;
    }

    @NotNull
    public List<PlatformDependency> getPlatformDependencies() {
        return platformDependencies;
    }

    public void setPlatformDependencies(List<PlatformDependency> platformDependencies) {
        this.platformDependencies = platformDependencies;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }

    public boolean isUnstable() {
        return unstable;
    }

    public void setUnstable(boolean unstable) {
        this.unstable = unstable;
    }

    @NotNull
    public String getVersionString() {
        return versionString;
    }

    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }

    @NotNull
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "NewVersion{" +
                "channel=" + channel +
                ", versionDependencies=" + versionDependencies +
                ", externalUrl='" + externalUrl + '\'' +
                ", forumSync=" + forumSync +
                ", platformDependencies=" + platformDependencies +
                ", recommended=" + recommended +
                ", unstable=" + unstable +
                ", versionString='" + versionString + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewVersion that = (NewVersion) o;
        return forumSync == that.forumSync &&
                recommended == that.recommended &&
                unstable == that.unstable &&
                channel.equals(that.channel) &&
                versionDependencies.equals(that.versionDependencies) &&
                Objects.equals(externalUrl, that.externalUrl) &&
                platformDependencies.equals(that.platformDependencies) &&
                versionString.equals(that.versionString) &&
                content.equals(that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, versionDependencies, externalUrl, forumSync, platformDependencies, recommended, unstable, versionString, content);
    }

}
