package io.papermc.hangar.config.hangar;

import io.papermc.hangar.model.common.Color;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "hangar.channels")
public record ChannelsConfig(
    @Min(1) @DefaultValue("15") int maxNameLen,
    @DefaultValue("^[a-zA-Z0-9]+$") String nameRegex,
    @DefaultValue("cyan") Color colorDefault,
    @Size(min = 1, max = 15) @DefaultValue("Release") String nameDefault
) {

    public boolean isValidChannelName(String name) {
        return name.length() >= 1 && name.length() <= maxNameLen && name.matches(nameRegex);
    }
}
