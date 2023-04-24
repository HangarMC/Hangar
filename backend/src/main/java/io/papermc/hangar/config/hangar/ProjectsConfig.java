package io.papermc.hangar.config.hangar;

import io.papermc.hangar.model.internal.api.responses.Validation;
import io.papermc.hangar.util.PatternWrapper;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;

@ConfigurationProperties(prefix = "hangar.projects")
public record ProjectsConfig( // TODO split into ProjectsConfig and VersionsConfig
                              @DefaultValue("^[a-zA-Z0-9-_]{3,}$") PatternWrapper nameRegex,
                              @DefaultValue("^[a-zA-Z0-9-_.+]+$") PatternWrapper versionNameRegex,
                              @DefaultValue("^[a-zA-Z0-9-_.() +]*$") PatternWrapper licenseNameRegex,
                              @DefaultValue("28") int maxNameLen,
                              @DefaultValue("30") int maxVersionNameLen,
                              @DefaultValue("15") int maxLicenseNameLen,
                              @DefaultValue("100") int maxDependencies,
                              @DefaultValue("50") int maxPages,
                              @DefaultValue("5") int maxChannels,
                              @DefaultValue("40000") int maxBBCodeLen,
                              @DefaultValue("25") int initLoad,
                              @DefaultValue("10") int initVersionLoad,
                              @DefaultValue("120") int maxDescLen,
                              @DefaultValue("1500") int maxSponsorsLen,
                              @DefaultValue("16") int maxKeywordLen,
                              @DefaultValue("5") int maxKeywords,
                              @DefaultValue("5") int maxTags,
                              @DefaultValue("1000000") int contentMaxLen,
                              @DefaultValue("7000000") int maxFileSize,
                              @DefaultValue("15000000") int maxTotalFilesSize,
                              @DefaultValue("true") boolean fileValidate, // TODO implement or remove
                              @DefaultValue("28") @DurationUnit(ChronoUnit.DAYS) Duration staleAge,
                              @DefaultValue("1h") String checkInterval, // TODO implement or remove
                              @DefaultValue("1d") String draftExpire, // TODO implement or remove
                              @DefaultValue("30") int userGridPageSize, // TODO implement or remove
                              @DefaultValue("10") @DurationUnit(ChronoUnit.MINUTES) Duration unsafeDownloadMaxAge,
                              @DefaultValue("false") boolean showUnreviewedDownloadWarning
) {

    public Validation projectName() {
        return new Validation(this.nameRegex(), this.maxNameLen(), null);
    }

    public Validation licenseName() {
        return new Validation(this.licenseNameRegex(), this.maxLicenseNameLen(), null);
    }

    public Validation projectDescription() {
        return Validation.max(this.maxDescLen());
    }

    public Validation projectKeywords() {
        return Validation.max(this.maxKeywords());
    }

    public Validation projectKeywordName() {
        return Validation.max(this.maxKeywordLen());
    }

    public Validation projectTags() {
        return Validation.max(this.maxTags());
    }

    public Validation versionName() {
        return new Validation(this.versionNameRegex(), this.maxVersionNameLen(), null);
    }

    public Validation sponsorsContent() {
        return Validation.max(this.maxSponsorsLen());
    }

    public int maxFileSizeMB() {
        return this.maxFileSize() / 1000 / 1000;
    }

    public int maxTotalFilesSizeMB() {
        return this.maxTotalFilesSize() / 1000 / 1000;
    }
}
