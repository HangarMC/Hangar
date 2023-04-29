package io.papermc.hangar.model.internal.api.requests.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.common.ChannelFlag;
import io.papermc.hangar.model.common.Color;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public class ChannelForm {

    // @el(root: String)
    @NotBlank
    private final @Validate(SpEL = "@validations.regex(#root, @hangarConfig.channels.nameRegex)", message = "channel.modal.error.invalidName") @Validate(SpEL = "@validations.max(#root, @hangarConfig.channels.maxNameLen)", message = "channel.modal.error.tooLongName") String name;
    private final @Validate(SpEL = "@validations.max(#root, @hangarConfig.channels.maxDescriptionLen)", message = "channel.modal.error.tooLongDescription") String description;
    @NotNull
    private final Color color;
    private final Set<ChannelFlag> flags;

    @JsonCreator
    public ChannelForm(final String name, final String description, final Color color, final Set<ChannelFlag> flags) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.flags = flags;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
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
            ", description=" + this.description +
            ", color=" + this.color +
            ", flags=" + this.flags +
            '}';
    }
}
