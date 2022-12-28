package io.papermc.hangar.config.hangar;

import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.internal.api.responses.Validation;
import io.papermc.hangar.util.PatternWrapper;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "hangar.channels")
public record ChannelsConfig(
    @Min(1) @DefaultValue("15") int maxNameLen,
    @DefaultValue("^[a-zA-Z0-9]+$") PatternWrapper nameRegex,
    @DefaultValue("cyan") Color colorDefault,
    @Size(min = 1, max = 15) @DefaultValue("Release") String nameDefault
) {

    public boolean isValidChannelName(final String name) {
        return name.length() >= 1 && name.length() <= this.maxNameLen() && this.nameRegex().test(name);
    }

    public Validation channelName() {
        return new Validation(this.nameRegex(), this.maxNameLen(), null);
    }
}
