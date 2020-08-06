package me.minidigger.hangar.service.plugindata;

import me.minidigger.hangar.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PluginFileWithData {
    private final Path path;
    private final PluginFileData data;
    private final long userId;

    public PluginFileWithData(Path path, PluginFileData data, long userId) {
        this.path = path;
        this.data = data;
        this.userId = userId;
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

    public String getMd5() {
        try {
            return StringUtils.md5ToHex(Files.readAllBytes(this.path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
