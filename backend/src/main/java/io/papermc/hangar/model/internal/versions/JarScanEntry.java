package io.papermc.hangar.model.internal.versions;

import org.jspecify.annotations.Nullable;

public record JarScanEntry(String severity, @Nullable String checkName, String message, String location) {
}
