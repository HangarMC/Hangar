package io.papermc.hangar.config.hangar;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.internal.api.responses.Validation;
import io.papermc.hangar.util.PatternWrapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;

@ConfigurationProperties(prefix = "hangar.orgs")
public record OrganizationsConfig(
    @DefaultValue("true") boolean enabled,
    @DefaultValue("org.papermc.io") String dummyEmailDomain,
    @DefaultValue("5") int createLimit,
    @DefaultValue("3") int minNameLen,
    @DefaultValue("20") int maxNameLen,
    @DefaultValue("^[a-zA-Z0-9-_]+$") PatternWrapper nameRegex
) {

    public boolean testOrgName(final String name) {
        if (name.length() > this.maxNameLen() || name.length() < this.minNameLen() || !this.nameRegex().test(name)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "organization.new.error.invalidName");
        }
        return true;
    }

    public Validation orgName() {
        return new Validation(this.nameRegex(), this.maxNameLen(), this.minNameLen());
    }
}
