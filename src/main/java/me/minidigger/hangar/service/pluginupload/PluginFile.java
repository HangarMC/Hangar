package me.minidigger.hangar.service.pluginupload;

import java.nio.file.Path;

import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.service.plugindata.PluginFileData;

public class PluginFile {

    private final Path path;
    private final UsersTable user;

    public PluginFile(Path path, UsersTable user) {
        this.path = path;
        this.user = user;
    }

    public void loadMeta() {

    }
}
