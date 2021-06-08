package io.papermc.hangar.service.internal.versions.plugindata;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.plugindata.handler.FileTypeHandler;
import io.papermc.hangar.service.internal.versions.plugindata.handler.FileTypeHandler.FileData;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

@Service
public class PluginDataService {

    private final Map<String, FileTypeHandler<? extends FileData>> pluginFileTypeHandlers = new HashMap<>();

    @Autowired
    public PluginDataService(List<FileTypeHandler<?>> fileTypeHandlers) {
        for (FileTypeHandler<?> fileTypeHandler : fileTypeHandlers) {
            this.pluginFileTypeHandlers.put(fileTypeHandler.getFileName(), fileTypeHandler);
        }
    }

    @NotNull
    public PluginFileWithData loadMeta(Path file, long userId) throws IOException {
        try (JarInputStream jarInputStream = openJar(file)) {

            Map<Platform, FileData> fileDataMap = new EnumMap<>(Platform.class);

            JarEntry jarEntry;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null && fileDataMap.size() < Platform.getValues().length) {
                FileTypeHandler<?> fileTypeHandler = pluginFileTypeHandlers.get(jarEntry.getName());
                if (fileTypeHandler != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(jarInputStream));
                    FileData fileData = fileTypeHandler.getData(reader);
                    fileDataMap.put(fileTypeHandler.getPlatform(), fileData);
                }
            }

            if (fileDataMap.isEmpty() ) {
                throw new HangarApiException("version.new.error.metaNotFound");
            }
            else {
                fileDataMap.forEach((platform, fileData) -> {
                    if (fileData.getVersion() == null && fileData.getName() == null && fileData.getPluginDependencies().isEmpty()) {
                        throw new HangarApiException("version.new.error.metaNotFound");
                    }
                });
                PluginFileWithData fileData = new PluginFileWithData(file, new PluginFileData(fileDataMap), userId);
                fileData.getData().validate();
                return fileData;
            }
        }
    }

    public JarInputStream openJar(Path file) throws IOException {
        if (file.toString().endsWith(".jar")) {
            return new JarInputStream(Files.newInputStream(file));
        } else {
            ZipFile zipFile = new ZipFile(file.toFile()); // gets closed by closing the input stream?
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                if (!zipEntry.isDirectory() && name.split("/").length == 1 && name.endsWith(".jar")) {
                    return new JarInputStream(zipFile.getInputStream(zipEntry));
                }
            }

            throw new HangarApiException("version.new.error.jarNotFound");
        }
    }
}
