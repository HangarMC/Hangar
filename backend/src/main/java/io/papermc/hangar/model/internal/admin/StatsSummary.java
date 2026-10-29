package io.papermc.hangar.model.internal.admin;

import java.util.List;

public record StatsSummary(StatsTotals totals, List<PlatformDownloads> platformDownloads, List<TopProject> topProjects) {
}
