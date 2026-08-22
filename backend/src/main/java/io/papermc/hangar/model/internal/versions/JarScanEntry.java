package io.papermc.hangar.model.internal.versions;

import org.checkerframework.checker.nullness.qual.Nullable;

public record JarScanEntry(String severity, @Nullable String checkName, String message, String location) {
}
