package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "hangar.orgs")
public record OrganizationsConfig(
    @DefaultValue("true") boolean enabled,
    @DefaultValue("org.papermc.io") String dummyEmailDomain,
    @DefaultValue("5") int createLimit,
    @DefaultValue("3") int minNameLen,
    @DefaultValue("20") int maxNameLen,
    @DefaultValue("[a-zA-Z0-9-_]*") String nameRegex
) {
}
