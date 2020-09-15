package io.papermc.hangar.service.plugindata.handler;

import io.papermc.hangar.model.Platform;
import io.papermc.hangar.service.plugindata.DataValue;

import java.io.BufferedReader;
import java.util.List;

public abstract class FileTypeHandler {

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
