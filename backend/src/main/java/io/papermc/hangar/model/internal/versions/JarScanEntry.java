package io.papermc.hangar.model.internal.versions;


import java.time.OffsetDateTime;
import org.jspecify.annotations.Nullable;

public record JarScanEntry(long id, String severity, @Nullable String checkName, String message, String location, boolean checked, @Nullable Long checkedBy, @Nullable OffsetDateTime checkedAt) {
}
