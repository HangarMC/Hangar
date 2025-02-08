package io.papermc.hangar.model.api.project.version;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.AtLeastOneNotNull;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.Platform;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jetbrains.annotations.NotNull;

@AtLeastOneNotNull(fieldNames = {"name", "namespace"}, includeBlankStrings = true, message = "Must specify a name or namespace for a dependency")
public class PluginDependency implements Named, Comparable<PluginDependency> {

    @Schema(description = "Name of the plugin dependency. For non-external dependencies, this should be the Hangar project name", example = "Maintenance")
    private final String name;
    @Schema(description = "Project ID of the dependency. Only for non-external dependencies", example = "1")
    private final Long projectId;
    @Schema(description = "Whether the dependency is required for the plugin to function")
    private final boolean required;
    @Schema(description = "External url to download the dependency from if not a Hangar project, else null", example = "https://papermc.io/downloads")
    private final @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "fieldError.url") String externalUrl;
    @Schema(description = "Platform the dependency runs on")
    private final Platform platform;

    @JdbiConstructor
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PluginDependency(final String name, final Long projectId, final boolean required, @Nested("pn") @Deprecated(forRemoval = true) final @Nullable ProjectNamespace namespace, final @Nullable String externalUrl, final Platform platform) {
        // TODO Remove ProjectNamespace and always require name
        if (name == null && namespace == null) {
            throw new IllegalArgumentException("Must specify a name for a dependency");
        }
        this.name = name != null ? name : namespace.getSlug();
        this.projectId = projectId;
        this.required = required;
        this.externalUrl = externalUrl;
        this.platform = platform;
    }

    private PluginDependency(final String name, final boolean required, final Platform platform) {
        this.name = name;
        this.projectId = null;
        this.required = required;
        this.externalUrl = null;
        this.platform = platform;
    }

    public Long getProjectId() {
        return this.projectId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public boolean isRequired() {
        return this.required;
    }

    public @Nullable String getExternalUrl() {
        return this.externalUrl;
    }

    public Platform getPlatform() {
        return this.platform;
    }

    @Override
    public String toString() {
        return "PluginDependency{" +
                "projectId=" + this.projectId +
                ", name='" + this.name + '\'' +
                ", required=" + this.required +
                ", externalUrl='" + this.externalUrl + '\'' +
                ", platform=" + this.platform +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final PluginDependency that = (PluginDependency) o;
        return this.required == that.required && Objects.equals(this.name, that.name) && Objects.equals(this.externalUrl, that.externalUrl) && Objects.equals(this.platform, that.platform);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.required, this.externalUrl, this.platform);
    }

    public static PluginDependency of(final String name, final boolean required, final Platform platform) {
        return new PluginDependency(name, required, platform);
    }

    @Override
    public int compareTo(final @NotNull PluginDependency o) {
        return this.name.compareTo(o.name);
    }
}
