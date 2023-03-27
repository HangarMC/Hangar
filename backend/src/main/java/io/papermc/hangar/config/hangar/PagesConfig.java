package io.papermc.hangar.config.hangar;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.internal.api.responses.Validation;
import io.papermc.hangar.util.PatternWrapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;

@ConfigurationProperties(prefix = "hangar.pages")
public record PagesConfig(
    @NestedConfigurationProperty Home home,
    @DefaultValue("^[a-zA-Z0-9-_ ]+$") PatternWrapper nameRegex,
    @DefaultValue("3") int minNameLen,
    @DefaultValue("25") int maxNameLen,
    @DefaultValue("75") int maxSlugLen,
    @DefaultValue("4") int maxNestingLevel,
    @DefaultValue("15") int minLen,
    @DefaultValue("32000") int maxLen
) {

    public void testPageName(final String name) {
        if (name.length() > this.maxNameLen) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.name.maxLength");
        } else if (name.length() < this.minNameLen) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.name.minLength");
        } else if (!this.nameRegex().test(name)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.name.invalidChars");
        }
    }

    public Validation pageName() {
        return new Validation(this.nameRegex(), this.maxNameLen(), this.minNameLen());
    }

    public Validation pageContent() {
        return Validation.size(this.maxLen(), this.minLen());
    }

    @ConfigurationProperties(prefix = "hangar.pages.home")
    public record Home(
        @DefaultValue("Resource Page") String name,
        @DefaultValue("Welcome to your new project!") String message
    ) {
    }
}
