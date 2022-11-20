package io.papermc.hangar.config.hangar;

import io.papermc.hangar.model.internal.api.responses.Validation;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "hangar.users")
public record UserConfig(
    @DefaultValue("100") int maxTaglineLen,
    @DefaultValue({"Hangar_Admin", "Hangar_Mod"}) List<String> staffRoles
) {

    public Validation userTagline() {
        return Validation.max(this.maxTaglineLen());
    }
}
