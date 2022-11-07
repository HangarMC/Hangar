package io.papermc.hangar.config.hangar;

import io.papermc.hangar.exceptions.HangarApiException;
import java.util.regex.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;

@ConfigurationProperties(prefix = "hangar.pages")
public record PagesConfig(
    @NestedConfigurationProperty Home home,
    @DefaultValue("^[a-zA-Z0-9-_ ]+$") String nameRegex,
    @DefaultValue("3") int minNameLen,
    @DefaultValue("25") int maxNameLen,
    @DefaultValue("15") int minLen,
    @DefaultValue("32000") int maxLen
) {

    @ConfigurationProperties(prefix = "hangar.pages.home")
    public record Home(
        @DefaultValue("Home") String name,
        @DefaultValue("Welcome to your new project!") String message
    ) {
    }

    public void testPageName(String name) {
        if (name.length() > maxNameLen) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.name.maxLength");
        } else if (name.length() < minNameLen) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.name.minLength");
        } else if (!Pattern.compile(nameRegex).matcher(name).matches()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.name.invalidChars");
        }
    }
}
