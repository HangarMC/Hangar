package io.papermc.hangar.service.plugindata;

import io.papermc.hangar.model.Platform;
import io.papermc.hangar.util.CryptoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PluginFileWithData {
    private final Path path;
    private final PluginFileData data;
    private final long userId;
    private final Platform platform;

    public PluginFileWithData(Path path, PluginFileData data, long userId, Platform platform) {
        this.path = path;
        this.data = data;
        this.userId = userId;
        this.platform = platform;
    }

    public Path getPath() {
        return path;
    }

    public PluginFileData getData() {
        return data;
    }

    public long getUserId() {
        return userId;
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getMd5() {
        try {
            return CryptoUtils.md5ToHex(Files.readAllBytes(this.path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
