package io.papermc.hangar.service.internal.versions.plugindata.handler;

import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import org.jdbi.v3.core.internal.MemoizingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.io.BufferedReader;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

public abstract class FileTypeHandler<D extends FileTypeHandler.FileData> {

    private final String fileName;
    private final Platform platform;

    protected FileTypeHandler(String fileName, Platform platform) {
        this.fileName = fileName;
        this.platform = platform;
    }

    @NotNull
    public String getFileName() {
        return fileName;
    }

    @NotNull
    public Platform getPlatform() {
        return platform;
    }

    public abstract D getData(BufferedReader reader) throws ConfigurateException;

    public abstract static class FileData {

        private String version;
        private String name;
        private String description;
        private String author;
        private Set<String> authors;

        private transient final MemoizingSupplier<Set<PluginDependency>> pluginDependencies = MemoizingSupplier.of(this::createPluginDependencies);

        @Nullable
        public final String getVersion() {
            return this.version;
        }

        @Nullable
        public final String getName() {
            return this.name;
        }

        @Nullable
        public final String getDescription() {
            return this.description;
        }

        @NotNull
        protected abstract Set<PluginDependency> createPluginDependencies();

        @NotNull
        public final Set<PluginDependency> getPluginDependencies() {
            return this.pluginDependencies.get();
        }

        @NotNull
        public SortedSet<String> getPlatformDependencies() {
            return Collections.emptySortedSet();
        }

        @NotNull
        public final Set<String> getAuthors() {
            if (this.author != null) {
                return Set.of(author);
            }
            return this.authors == null ? Collections.emptySet() : this.authors;
        }

        @Override
        public final String toString() {
            return "FileData{" +
                    "version='" + version + '\'' +
                    ", name='" + name + '\'' +
                    ", pluginDependencies='" + getPluginDependencies() + '\'' +
                    '}';
        }
    }
}
