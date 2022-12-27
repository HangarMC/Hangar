package io.papermc.hangar.service.internal.versions.plugindata;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.plugindata.handler.FileTypeHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PluginDataService {

    private final Map<String, FileTypeHandler<? extends FileTypeHandler.FileData>> pluginFileTypeHandlers = new HashMap<>();

    @Autowired
    public PluginDataService(final List<FileTypeHandler<?>> fileTypeHandlers) {
        for (final FileTypeHandler<?> fileTypeHandler : fileTypeHandlers) {
            this.pluginFileTypeHandlers.put(fileTypeHandler.getFileName(), fileTypeHandler);
        }
    }

    public @NotNull PluginFileWithData loadMeta(final Path file, final long userId) throws IOException {
        try (final JarInputStream jarInputStream = this.openJar(file)) {
            final Map<Platform, FileTypeHandler.FileData> fileDataMap = new EnumMap<>(Platform.class);

            JarEntry jarEntry;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null && fileDataMap.size() < Platform.getValues().length) {
                final FileTypeHandler<?> fileTypeHandler = this.pluginFileTypeHandlers.get(jarEntry.getName());
                if (fileTypeHandler != null) {
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(jarInputStream));
                    final FileTypeHandler.FileData fileData = fileTypeHandler.getData(reader);
                    fileDataMap.put(fileTypeHandler.getPlatform(), fileData);
                }
            }

            fileDataMap.forEach((platform, fileData) -> {
                if (fileData.getVersion() == null && fileData.getName() == null && fileData.getPluginDependencies().isEmpty()) {
                    throw new HangarApiException("version.new.error.metaNotFound");
                }
            });
            return new PluginFileWithData(file, new PluginFileData(fileDataMap), userId);
        }
    }

    public JarInputStream openJar(final Path file) throws IOException {
        if (file.toString().endsWith(".jar")) {
            return new JarInputStream(Files.newInputStream(file));
        } else {
            final ZipFile zipFile = new ZipFile(file.toFile()); // gets closed by closing the input stream?
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                final ZipEntry zipEntry = entries.nextElement();
                final String name = zipEntry.getName();
                if (!zipEntry.isDirectory() && name.split("/").length == 1 && name.endsWith(".jar")) {
                    return new JarInputStream(zipFile.getInputStream(zipEntry));
                }
            }

            throw new HangarApiException("version.new.error.jarNotFound");
        }
    }
}
