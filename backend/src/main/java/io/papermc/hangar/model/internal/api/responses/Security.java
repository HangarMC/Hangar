package io.papermc.hangar.model.internal.api.responses;

import java.util.List;

public record Security(List<String> safeDownloadHosts) {
}
