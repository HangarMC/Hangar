package io.papermc.hangar.serviceold.pluginupload;

import io.papermc.hangar.db.modelold.UsersTable;

import java.nio.file.Path;

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
