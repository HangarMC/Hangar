package io.papermc.hangar.components.webhook.model.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.concurrent.ThreadLocalRandom;

@JsonTypeInfo(use = JsonTypeInfo.Id.SIMPLE_NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ProjectPublishedEvent.class, name = ProjectPublishedEvent.TYPE),
    @JsonSubTypes.Type(value = VersionPublishedEvent.class, name = VersionPublishedEvent.TYPE),
})
public abstract class WebhookEvent {

    private final String id;
    private final String type;

    protected WebhookEvent(final String type) {
        this.id = String.valueOf(ThreadLocalRandom.current().nextInt());
        this.type = type;
    }

    public String getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }
}
