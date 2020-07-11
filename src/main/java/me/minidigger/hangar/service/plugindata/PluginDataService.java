package me.minidigger.hangar.service.plugindata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import me.minidigger.hangar.service.plugindata.handler.FileTypeHandler;
import me.minidigger.hangar.util.HangarException;

import static me.minidigger.hangar.service.plugindata.DataValue.*;

@Service
public class PluginDataService {

    private final Map<String, FileTypeHandler> fileTypeHandlers = new HashMap<>();

    @Autowired
    public PluginDataService(List<FileTypeHandler> fileTypeHandlers) {
        for (FileTypeHandler fileTypeHandler : fileTypeHandlers) {
            this.fileTypeHandlers.put(fileTypeHandler.getFileName(), fileTypeHandler);
        }
    }

    public PluginFileData loadMeta(Path file) throws IOException {
        try (JarInputStream jarInputStream = openJar(file)) {

            List<DataValue> dataValues = new ArrayList<>();

            JarEntry jarEntry;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                FileTypeHandler fileTypeHandler = fileTypeHandlers.get(jarEntry.getName());
                if (fileTypeHandler != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(jarInputStream));
                    List<DataValue> data = fileTypeHandler.getData(reader);
                    dataValues.addAll(data);
                }
            }

            if (dataValues.isEmpty() || dataValues.size() == 1) { // 1 = only dep was found = useless
                throw new HangarException("error.plugin.metaNotFound");
            } else {
                dataValues.add(new UUIDDataValue("id", UUID.randomUUID()));
                PluginFileData fileData = new PluginFileData(dataValues);
                if (!fileData.validate()) {
                    throw new HangarException("error.plugin.incomplete", "id or version");
                }
                return fileData;
            }
        }
    }

    private JarInputStream openJar(Path file) throws IOException {
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

            throw new HangarException("error.plugin.jarNotFound");
        }
    }
}
