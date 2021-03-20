package io.papermc.hangar.model.internal.api.requests.projects;

import io.papermc.hangar.model.common.Color;

public class EditChannelForm extends ChannelForm {

    private final long id;

    public EditChannelForm(String name, Color color, boolean nonReviewed, long id) {
        super(name, color, nonReviewed);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "EditChannelForm{" +
                "id=" + id +
                "} " + super.toString();
    }
}
