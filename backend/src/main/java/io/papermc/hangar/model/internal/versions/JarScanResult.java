package io.papermc.hangar.model.internal.versions;

import io.papermc.hangar.model.common.Platform;
import java.time.OffsetDateTime;
import java.util.List;

public record JarScanResult(long id, Platform platform, OffsetDateTime createdAt, String highestSeverity, List<String> entries) {
}
