package io.papermc.hangar.components.webhook.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import java.util.List;

public record DiscordWebhook(
    @JsonProperty("avatar_url") String avatarUrl,
    @JsonRawValue String embeds,
    @JsonProperty("allowed_mentions") AllowedMentions allowedMentions
) {

    public DiscordWebhook(final String embeds) {
        this("https://hangar.papermc.io/hangar-logo.png", embeds, new AllowedMentions());
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
