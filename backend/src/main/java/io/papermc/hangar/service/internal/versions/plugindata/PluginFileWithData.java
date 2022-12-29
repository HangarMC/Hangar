package io.papermc.hangar.service.internal.versions.plugindata;

public record PluginFileWithData(String fileName, int fileSize, String md5, PluginFileData data, long userId) {}
