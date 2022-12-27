package io.papermc.hangar.model.internal.api.requests.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.common.ChannelFlag;
import io.papermc.hangar.model.common.Color;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ChannelForm {

    // @el(root: String)
    private final @NotBlank @Validate(SpEL = "@validations.regex(#root, @hangarConfig.channels.nameRegex)", message = "channel.modal.error.invalidName") @Validate(SpEL = "@validations.max(#root, @hangarConfig.channels.maxNameLen)", message = "channel.modal.error.tooLongName") String name;
    private final @NotNull Color color;
    private final Set<ChannelFlag> flags;

    @JsonCreator
    public ChannelForm(final String name, final Color color, final Set<ChannelFlag> flags) {
        this.name = name;
        this.color = color;
        this.flags = flags;
    }

    public String getName() {
        return this.name;
    }

    public Color getColor() {
        return this.color;
    }

    public Set<ChannelFlag> getFlags() {
        return this.flags;
    }

    @Override
    public String toString() {
        return "ChannelForm{" +
            "name='" + this.name + '\'' +
            ", color=" + this.color +
            ", flags=" + this.flags +
            '}';
    }
}
