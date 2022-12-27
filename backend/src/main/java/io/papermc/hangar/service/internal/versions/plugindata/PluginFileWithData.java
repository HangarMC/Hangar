package io.papermc.hangar.service.internal.versions.plugindata;

import io.papermc.hangar.util.CryptoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PluginFileWithData {
    private final Path path;
    private final PluginFileData data;
    private final long userId;

    public PluginFileWithData(final Path path, final PluginFileData data, final long userId) {
        this.path = path;
        this.data = data;
        this.userId = userId;
    }

    public Path getPath() {
        return this.path;
    }

    public PluginFileData getData() {
        return this.data;
    }

    public long getUserId() {
        return this.userId;
    }

    public String getMd5() {
        try {
            return CryptoUtils.md5ToHex(Files.readAllBytes(this.path));
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
