package io.papermc.hangar.components.webhook.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import java.util.List;

public record DiscordWebhook(
    String username,
    String avatarUrl,
    @JsonRawValue String embeds,
    AllowedMentions allowedMentions
) {

    public DiscordWebhook(final String embeds) {
        this("HangarBot", "https://hangar.papermc.dev/hangar-logo.png", embeds, new AllowedMentions());
    }

    record AllowedMentions(
        List<String> parse,
        List<Long> roles,
        List<Long> users,
        boolean repliedUser
    ) {

        AllowedMentions() {
            this(List.of(), null, null, false);
        }
    }
}
