package io.papermc.hangar.service.internal.versions.plugindata.handler;

import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.plugindata.handler.VelocityFileTypeHandler.VelocityFileData;
import java.io.BufferedReader;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.springframework.stereotype.Component;

@Component
public class VelocityFileTypeHandler extends FileTypeHandler<VelocityFileData> {

    protected VelocityFileTypeHandler() {
        super("velocity-plugin.json", Platform.VELOCITY);
    }

    @Override
    public VelocityFileData getData(final BufferedReader reader) throws ConfigurateException {
        return JacksonConfigurationLoader.builder().buildAndLoadString(reader.lines().collect(Collectors.joining("\n"))).get(VelocityFileData.class);
    }

    @ConfigSerializable
    public static class VelocityFileData extends FileTypeHandler.FileData {

        private Set<Dependency> dependencies;

        @Override
        protected @NotNull Set<PluginDependency> createPluginDependencies() {
            return this.dependencies.stream().map(dependency -> PluginDependency.of(dependency.id, !dependency.optional, Platform.VELOCITY)).collect(Collectors.toSet());
        }

        @ConfigSerializable
        static class Dependency {

            private String id;
            private boolean optional;

            @Override
            public boolean equals(final Object o) {
                if (this == o) return true;
                if (o == null || this.getClass() != o.getClass()) return false;
                final Dependency that = (Dependency) o;
                return this.optional == that.optional && this.id.equals(that.id);
            }

            @Override
            public int hashCode() {
                return Objects.hash(this.id, this.optional);
            }
        }
    }
}
