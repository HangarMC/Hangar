package io.papermc.hangar.model.internal.api.responses;

import java.util.List;
import java.util.Set;

public record Security(List<String> safeDownloadHosts, Set<String> oauthProviders) {
}
