package io.papermc.hangar.model.internal.api.responses;

import io.papermc.hangar.config.hangar.HangarConfig;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public record Validations(
    Project project,
    Validation userTagline,
    Validation version,
    Validation org,
    int maxOrgCount,
    String urlRegex
) {

    public record Project(
        Validation name,
        Validation desc,
        Validation license,
        Validation keywords,
        Validation channels,
        Validation pageName,
        Validation pageContent,
        Validation sponsorsContent,
        int maxPageCount,
        int maxChannelCount
    ) {
    }

    public static Validations create(final HangarConfig config) {
        final Project project = new Project(
            config.projects.projectName(),
            config.projects.projectDescription(),
            config.projects.licenseName(),
            config.projects.projectKeywords(),
            config.channels.channelName(),
            config.pages.pageName(),
            config.pages.pageContent(),
            config.projects.sponsorsContent(),
            config.projects.maxPages(),
            config.projects.maxChannels()
        );
        return new Validations(
            project,
            config.user.userTagline(),
            config.projects.versionName(),
            config.org.orgName(),
            config.org.createLimit(),
            config.getUrlRegex()
        );
    }
}
