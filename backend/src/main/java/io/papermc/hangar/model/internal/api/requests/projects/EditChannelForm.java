package io.papermc.hangar.model.internal.api.requests.projects;

import io.papermc.hangar.model.common.ChannelFlag;
import io.papermc.hangar.model.common.Color;
import java.util.Set;

public class EditChannelForm extends ChannelForm {

    private final long id;

    public EditChannelForm(final String name, final String description, final Color color, final Set<ChannelFlag> flags, final long id) {
        super(name, description, color, flags);
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return "EditChannelForm{" +
            "id=" + this.id +
            "} " + super.toString();
    }
}
