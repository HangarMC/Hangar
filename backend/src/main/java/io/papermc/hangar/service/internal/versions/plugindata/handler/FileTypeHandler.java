package io.papermc.hangar.service.internal.versions.plugindata.handler;

import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import java.io.BufferedReader;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import org.jdbi.v3.core.internal.MemoizingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

public abstract class FileTypeHandler<D extends FileTypeHandler.FileData> {

    private final String fileName;
    private final Platform platform;

    protected FileTypeHandler(final String fileName, final Platform platform) {
        this.fileName = fileName;
        this.platform = platform;
    }

    public @NotNull String getFileName() {
        return this.fileName;
    }

    public @NotNull Platform getPlatform() {
        return this.platform;
    }

    public abstract D getData(BufferedReader reader) throws ConfigurateException;

    public abstract static class FileData {

        private String version;
        private String name;
        private String description;
        private String author;
        private Set<String> authors;

        @SuppressWarnings("java:S2065")
        private final transient MemoizingSupplier<Set<PluginDependency>> pluginDependencies = MemoizingSupplier.of(this::createPluginDependencies);

        public final @Nullable String getVersion() {
            return this.version;
        }

        public final @Nullable String getName() {
            return this.name;
        }

        public final @Nullable String getDescription() {
            return this.description;
        }

        protected abstract @NotNull Set<PluginDependency> createPluginDependencies();

        public final @NotNull Set<PluginDependency> getPluginDependencies() {
            return this.pluginDependencies.get();
        }

        public @NotNull SortedSet<String> getPlatformDependencies() {
            return Collections.emptySortedSet();
        }

        public final @NotNull Set<String> getAuthors() {
            if (this.author != null) {
                return Set.of(this.author);
            }
            return this.authors == null ? Collections.emptySet() : this.authors;
        }

        @Override
        public final String toString() {
            return "FileData{" +
                "version='" + this.version + '\'' +
                ", name='" + this.name + '\'' +
                ", pluginDependencies='" + this.getPluginDependencies() + '\'' +
                '}';
        }
    }
}
