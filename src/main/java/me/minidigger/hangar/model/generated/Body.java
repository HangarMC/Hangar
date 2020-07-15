package me.minidigger.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import javax.validation.Valid;

import io.swagger.annotations.ApiModelProperty;

/**
 * Body
 */
@Validated
public class Body {
    @JsonProperty("plugin-info")
    private DeployVersionInfo pluginInfo = null;

    @JsonProperty("plugin-file")
    private Resource pluginFile = null;

    public Body pluginInfo(DeployVersionInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
        return this;
    }

    /**
     * Get pluginInfo
     *
     * @return pluginInfo
     **/
    @ApiModelProperty(value = "")

    @Valid
    public DeployVersionInfo getPluginInfo() {
        return pluginInfo;
    }

    public void setPluginInfo(DeployVersionInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    public Body pluginFile(Resource pluginFile) {
        this.pluginFile = pluginFile;
        return this;
    }

    /**
     * The jar/zip file to upload
     *
     * @return pluginFile
     **/
    @ApiModelProperty(value = "The jar/zip file to upload")

    @Valid
    public Resource getPluginFile() {
        return pluginFile;
    }

    public void setPluginFile(Resource pluginFile) {
        this.pluginFile = pluginFile;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Body body = (Body) o;
        return Objects.equals(this.pluginInfo, body.pluginInfo) &&
               Objects.equals(this.pluginFile, body.pluginFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginInfo, pluginFile);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Body {\n");

        sb.append("    pluginInfo: ").append(toIndentedString(pluginInfo)).append("\n");
        sb.append("    pluginFile: ").append(toIndentedString(pluginFile)).append("\n");
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
