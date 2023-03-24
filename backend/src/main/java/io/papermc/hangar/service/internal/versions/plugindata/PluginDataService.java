package io.papermc.hangar.service.internal.versions.plugindata;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.plugindata.handler.FileTypeHandler;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import io.papermc.hangar.util.CryptoUtils;
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

    public @NotNull PluginFileWithData loadMeta(final String fileName, final byte[] bytes, final long userId) throws IOException {
        try (final Jar jar = this.openJar(fileName, new ByteArrayInputStream(bytes))) {
            final Map<Platform, FileTypeHandler.FileData> fileDataMap = new EnumMap<>(Platform.class);

            JarEntry jarEntry;
            while ((jarEntry = jar.stream().getNextJarEntry()) != null && fileDataMap.size() < Platform.getValues().length) {
                final FileTypeHandler<?> fileTypeHandler = this.pluginFileTypeHandlers.get(jarEntry.getName());
                if (fileTypeHandler != null) {
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(jar.stream()));
                    final FileTypeHandler.FileData fileData = fileTypeHandler.getData(reader);
                    fileDataMap.put(fileTypeHandler.getPlatform(), fileData);
                }
            }

            fileDataMap.forEach((platform, fileData) -> {
                if (fileData.getVersion() == null && fileData.getName() == null && fileData.getPluginDependencies().isEmpty()) {
                    throw new HangarApiException("version.new.error.metaNotFound");
                }
            });
            return new PluginFileWithData(jar.fileName(), bytes.length, CryptoUtils.sha256ToHex(bytes), new PluginFileData(fileDataMap), userId);
        }
    }

    public Jar openJar(final String fileName, final InputStream file) throws IOException {
        if (fileName.endsWith(".jar")) {
            return new Jar(fileName, new JarInputStream(file));
        } else {
            final ZipInputStream stream = new ZipInputStream(file);

            ZipEntry zipEntry;
            while ((zipEntry = stream.getNextEntry()) != null) {
                final String name = zipEntry.getName();
                if (!zipEntry.isDirectory() && name.split("/").length == 1 && name.endsWith(".jar")) {
                    // todo what about multiple jars in one zip?
                    return new Jar(zipEntry.getName(), new JarInputStream(stream));
                }
            }

            throw new HangarApiException("version.new.error.jarNotFound");
        }
    }

    record Jar(String fileName, JarInputStream stream) implements AutoCloseable {
        @Override
        public void close() throws IOException {
            this.stream.close();
        }
    }
}
