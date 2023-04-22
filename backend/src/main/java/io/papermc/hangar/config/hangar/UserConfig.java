package io.papermc.hangar.config.hangar;

import io.papermc.hangar.model.internal.api.responses.Validation;
import io.papermc.hangar.util.PatternWrapper;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "hangar.users")
public record UserConfig(
    @DefaultValue("100") int maxTaglineLen,
    @DefaultValue({"Hangar_Admin", "Hangar_Mod"}) List<String> staffRoles,
    @DefaultValue("30") int nameChangeInterval,
    @DefaultValue("90") int nameChangeHistory,
    @DefaultValue("20") int maxNameLen,
    @DefaultValue("^[a-zA-Z0-9-_]{3,}$") PatternWrapper nameRegex
) {

    public Validation userTagline() {
        return Validation.max(this.maxTaglineLen());
    }
}
