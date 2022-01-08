package io.papermc.hangar.service.internal.versions.plugindata.scanner;

import io.papermc.hangar.model.common.Platform;

public record ScanResult(Platform platform, boolean foundSomething) {

}
