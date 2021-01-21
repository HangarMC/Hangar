package io.papermc.hangar.service.plugindata.handler;

import io.papermc.hangar.modelold.Platform;
import io.papermc.hangar.service.plugindata.DataValue;

import java.io.BufferedReader;
import java.util.List;

public abstract class FileTypeHandler {

    public static final String VERSION = "version";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String URL = "url";
    public static final String AUTHORS = "authors";
    public static final String DEPENDENCIES = "dependencies";
    public static final String PLATFORM_DEPENDENCY = "platform-dependency";

    private final String fileName;

    protected FileTypeHandler(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public abstract List<DataValue> getData(BufferedReader reader);

    public abstract Platform getPlatform();
}
