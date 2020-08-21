package io.papermc.hangar.model.generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * ModelsProtocolsAPIV2VersionDependency
 */
@Validated
public class Dependency {
    @JsonProperty("plugin_id")
    private String pluginId = null;

    @JsonProperty("version")
    private String version = null;

    @JsonProperty("required")
    private boolean required = true;

    public Dependency(String pluginId, String version) {
        this(pluginId, version, true);
    }

    public Dependency(String pluginId, String version, boolean required) {
        this.pluginId = pluginId;
        this.version = version;
        this.required = required;
    }

    public Dependency() {
        //
    }

    /**
     * Get pluginId
     *
     * @return pluginId
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    /**
     * Get version
     *
     * @return version
     **/
    @ApiModelProperty(value = "")

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Dependency dependency = (Dependency) o;
        return Objects.equals(this.pluginId, dependency.pluginId) &&
               Objects.equals(this.version, dependency.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, version);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelsProtocolsAPIV2VersionDependency {\n");

        sb.append("    pluginId: ").append(toIndentedString(pluginId)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
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

    public static List<Dependency> from(List<String> dependencies) {
        return dependencies.stream().map(Dependency::fromString).collect(Collectors.toList());
    }

    private static Dependency fromString(String dep) {
        if (dep.contains(":")) {
            return new Dependency(dep.split(":")[0], dep.split(":")[1]);
        } else {
            return new Dependency(dep, null);
        }
    }

}
