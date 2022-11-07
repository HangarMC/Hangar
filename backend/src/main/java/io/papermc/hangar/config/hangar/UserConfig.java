package io.papermc.hangar.config.hangar;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "hangar.users")
public record UserConfig(
    @DefaultValue("100") int maxTaglineLen,
    @DefaultValue({"Hangar_Admin", "Hangar_Mod"}) List<String> staffRoles
) {
}
