package me.minidigger.hangar.service.plugindata.handler;

import java.io.BufferedReader;
import java.util.List;

import me.minidigger.hangar.service.plugindata.DataValue;

public abstract class FileTypeHandler {

    private final String fileName;

    protected FileTypeHandler(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public abstract List<DataValue> getData(BufferedReader reader);
}
