package io.papermc.hangar.config.hangar;

import io.papermc.hangar.util.PatternWrapper;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;

@ConfigurationProperties(prefix = "hangar.projects")
public record ProjectsConfig( // TODO split into ProjectsConfig and VersionsConfig
    @DefaultValue("^[a-zA-Z0-9-_]{3,}$") PatternWrapper nameRegex,
    @DefaultValue("^[a-zA-Z0-9-_.+]+$") PatternWrapper versionNameRegex,
    @DefaultValue("25") int maxNameLen,
    @DefaultValue("30") int maxVersionNameLen,
    @DefaultValue("100") int maxDependencies,
    @DefaultValue("50") int maxPages,
    @DefaultValue("5") int maxChannels,
    @DefaultValue("30000") int maxBBCodeLen,
    @DefaultValue("25") int initLoad,
    @DefaultValue("10") int initVersionLoad,
    @DefaultValue("120") int maxDescLen,
    @DefaultValue("500") int maxSponsorsLen,
    @DefaultValue("5") int maxKeywords,
    @DefaultValue("1000000") int contentMaxLen,
    @DefaultValue("true") boolean fileValidate, // TODO implement or remove
    @DefaultValue("28") @DurationUnit(ChronoUnit.DAYS) Duration staleAge,
    @DefaultValue("1h") String checkInterval, // TODO implement or remove
    @DefaultValue("1d") String draftExpire, // TODO implement or remove
    @DefaultValue("30") int userGridPageSize, // TODO implement or remove
    @DefaultValue("10") @DurationUnit(ChronoUnit.MINUTES) Duration unsafeDownloadMaxAge,
    @DefaultValue("false") boolean showUnreviewedDownloadWarning
) {
}
