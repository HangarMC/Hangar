package io.papermc.hangar.config.hangar;

import io.papermc.hangar.model.internal.api.responses.Validation;
import io.papermc.hangar.util.PatternWrapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "hangar.orgs")
public record OrganizationsConfig(
    @DefaultValue("true") boolean enabled,
    @DefaultValue("org.papermc.io") String dummyEmailDomain,
    @DefaultValue("5") int createLimit,
    @DefaultValue("3") int minNameLen,
    @DefaultValue("20") int maxNameLen,
    @DefaultValue("[a-zA-Z0-9-_]*") PatternWrapper nameRegex
) {

    public Validation orgName() {
        return new Validation(this.nameRegex(), this.maxNameLen(), this.minNameLen());
    }
}
