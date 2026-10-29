package io.papermc.hangar.components.discovery;

import io.papermc.hangar.config.hangar.DiscoveryConfig;
import io.papermc.hangar.model.api.project.ProjectCompact;
import java.time.LocalDate;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Service;

@Service
public class DiscoveryService {

    private static final int BUCKETS = 3;
    private static final long GOLDEN_GAMMA = 0x9E3779B97F4A7C15L;

    private final DiscoveryDAO discoveryDAO;
    private final DiscoveryConfig config;

    private volatile @Nullable LocalDate cachedDay;
    private volatile List<ProjectCompact> cachedPicks;

    public DiscoveryService(final DiscoveryDAO discoveryDAO, final DiscoveryConfig config) {
        this.discoveryDAO = discoveryDAO;
        this.config = config;
    }

    /**
     * The same set for everyone for the whole day, so the strip survives a refresh and a project someone saw in
     * the morning is still there in the evening.
     */
    public List<ProjectCompact> getDailyPicks() {
        final LocalDate today = LocalDate.now();
        final List<ProjectCompact> cached = this.cachedPicks;
        if (cached != null && today.equals(this.cachedDay)) {
            return cached;
        }

        final double cursor = cursorFor(today);
        final int perBucket = this.config.perBucket();
        List<ProjectCompact> picks = this.discoveryDAO.discover(cursor, perBucket, this.config.maxAgeDays());
        if (picks.size() < BUCKETS * perBucket) {
            picks = this.discoveryDAO.discover(cursor, perBucket, 0);
        }

        this.cachedPicks = picks;
        this.cachedDay = today;
        return picks;
    }

    /**
     * Keeps a project out of every future rotation, not only the current one. The slot it vacates is taken by the
     * next project in that bucket's rotation, so the row refills rather than coming back a card short.
     */
    public void exclude(final long projectId, final long userId) {
        this.discoveryDAO.exclude(projectId, userId);
        this.cachedDay = null;
    }

    public void include(final long projectId) {
        this.discoveryDAO.include(projectId);
        this.cachedDay = null;
    }

    public List<ExcludedProject> getExcluded() {
        return this.discoveryDAO.excluded();
    }

    // Scrambles the day number into [0, 1) so consecutive days land in unrelated parts of the seed space
    static double cursorFor(final LocalDate day) {
        return ((day.toEpochDay() * GOLDEN_GAMMA) >>> 11) / (double) (1L << 53);
    }
}
