package io.papermc.hangar.model.api.project.version;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.AtLeastOneNotNull;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.api.project.ProjectNamespace;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jetbrains.annotations.Nullable;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

@AtLeastOneNotNull(fieldNames = {"namespace", "externalUrl"}, includeBlankStrings = true, message = "Must specify a projectId or external URL for a dependency")
public class PluginDependency implements Named {

    @NotBlank(message = "Must have a dependency name")
    private final String name;
    private final boolean required;
    private final ProjectNamespace namespace;
    private final String externalUrl;

    @JdbiConstructor
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PluginDependency(String name, boolean required, @Nested("pn") @Nullable ProjectNamespace namespace, String externalUrl) {
        this.name = name;
        this.required = required;
        this.namespace = namespace;
        this.externalUrl = externalUrl;
    }

    private PluginDependency(String name, boolean required) {
        this.name = name;
        this.required = required;
        this.namespace = null;
        this.externalUrl = null;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    @Nullable
    public ProjectNamespace getNamespace() {
        return namespace;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    @Override
    public String toString() {
        return "PluginDependency{" +
                "name='" + name + '\'' +
                ", required=" + required +
                ", namespace=" + namespace +
                ", externalUrl='" + externalUrl + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginDependency that = (PluginDependency) o;
        return required == that.required && name.equals(that.name) && Objects.equals(namespace, that.namespace) && Objects.equals(externalUrl, that.externalUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, required, namespace, externalUrl);
    }

    public static PluginDependency of(String name, boolean required) {
        return new PluginDependency(name, required);
    }
}
