package io.papermc.hangar.model.internal.api.requests.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.common.Color;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ChannelForm {

    @NotBlank
    @Validate(SpEL = "@validations.regex(#root, @hangarConfig.channels.nameRegex)", message = "channel.modal.error.invalidName")
    @Validate(SpEL = "@validations.max(#root, @hangarConfig.channels.maxNameLen)", message = "channel.modal.error.tooLongName")
    private final String name;
    @NotNull
    private final Color color;
    private final boolean nonReviewed;

    @JsonCreator
    public ChannelForm(String name, Color color, boolean nonReviewed) {
        this.name = name;
        this.color = color;
        this.nonReviewed = nonReviewed;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public boolean isNonReviewed() {
        return nonReviewed;
    }

    @Override
    public String toString() {
        return "ChannelForm{" +
                "name='" + name + '\'' +
                ", color=" + color +
                ", nonReviewed=" + nonReviewed +
                '}';
    }
}
