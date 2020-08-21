package io.papermc.hangar.service.pluginupload;

import java.nio.file.Path;

import io.papermc.hangar.db.model.UsersTable;

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
