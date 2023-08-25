package io.papermc.hangar.model.api.project.version;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.AtLeastOneNotNull;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.common.Platform;
import java.util.Objects;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jetbrains.annotations.Nullable;

@AtLeastOneNotNull(fieldNames = {"namespace", "externalUrl"}, includeBlankStrings = true, message = "Must specify a namespace or external URL for a dependency")
@AtLeastOneNotNull(fieldNames = {"name", "namespace"}, includeBlankStrings = true, message = "Must specify a name or namespace for a dependency")
public class PluginDependency implements Named {

    private final String name;
    private final boolean required;
    private final ProjectNamespace namespace;
    private final String externalUrl;
    private final Platform platform;

    @JdbiConstructor
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PluginDependency(final @Nullable String name, final boolean required, @Nested("pn") final @Nullable ProjectNamespace namespace, final @Nullable String externalUrl, final @Nullable Platform platform) {
        this.name = namespace != null ? null : name;
        this.required = required;
        this.namespace = namespace;
        this.externalUrl = externalUrl;
        this.platform = platform;
    }

    private PluginDependency(final String name, final boolean required, final Platform platform) {
        this.name = name;
        this.required = required;
        this.namespace = null;
        this.externalUrl = null;
        this.platform = platform;
    }

    @Override
    public String getName() {
        return this.namespace != null ? this.namespace.getSlug() : this.name;
    }

    public boolean isRequired() {
        return this.required;
    }

    public @Nullable ProjectNamespace getNamespace() {
        return this.namespace;
    }

    public @Nullable String getExternalUrl() {
        return this.externalUrl;
    }

    public @Nullable Platform getPlatform(){ return this.platform; }

    @Override
    public String toString() {
        return "PluginDependency{" +
            "name='" + this.name + '\'' +
            ", required=" + this.required +
            ", namespace=" + this.namespace +
            ", externalUrl='" + this.externalUrl + '\'' +
            '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final PluginDependency that = (PluginDependency) o;
        return this.required == that.required && Objects.equals(this.name, that.name) && Objects.equals(this.namespace, that.namespace) && Objects.equals(this.externalUrl, that.externalUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.required, this.namespace, this.externalUrl);
    }

    public static PluginDependency of(final String name, final boolean required, final Platform platform) {
        return new PluginDependency(name, required, platform);
    }
}
