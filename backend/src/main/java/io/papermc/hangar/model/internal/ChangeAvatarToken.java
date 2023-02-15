package io.papermc.hangar.model.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record ChangeAvatarToken(String signedData, String targetUsername, int requestUserId) {

    @JsonCreator
    public static ChangeAvatarToken of(final ObjectNode objectNode) {
        final ObjectNode rawData = (ObjectNode) objectNode.get("raw_data");
        return new ChangeAvatarToken(
            objectNode.get("signed_data").asText(),
            rawData.get("target_username").asText(),
            rawData.get("request_user_id").asInt()
        );
    }
}
