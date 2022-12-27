package io.papermc.hangar.model.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ChangeAvatarToken {

    private final String signedData;
    private final String targetUsername;
    private final int requestUserId;

    public ChangeAvatarToken(final String signedData, final String targetUsername, final int requestUserId) {
        this.signedData = signedData;
        this.targetUsername = targetUsername;
        this.requestUserId = requestUserId;
    }

    public String getSignedData() {
        return this.signedData;
    }

    public String getTargetUsername() {
        return this.targetUsername;
    }

    public int getRequestUserId() {
        return this.requestUserId;
    }

    @JsonCreator
    public static ChangeAvatarToken of(final ObjectNode objectNode) {
        final ObjectNode rawData = (ObjectNode) objectNode.get("raw_data");
        return new ChangeAvatarToken(
                objectNode.get("signed_data").asText(),
                rawData.get("target_username").asText(),
                rawData.get("request_user_id").asInt()
        );
    }

    @Override
    public String toString() {
        return "ChangeAvatarToken{" +
                "signedData='" + this.signedData + '\'' +
                ", targetUsername='" + this.targetUsername + '\'' +
                ", requestUserId=" + this.requestUserId +
                '}';
    }
}
