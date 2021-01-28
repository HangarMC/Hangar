package io.papermc.hangar.serviceold.sso;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ChangeAvatarToken {

    private final String signedData;
    private final String targetUsername;
    private final int requestUserId;

    public ChangeAvatarToken(String signedData, String targetUsername, int requestUserId) {
        this.signedData = signedData;
        this.targetUsername = targetUsername;
        this.requestUserId = requestUserId;
    }

    public String getSignedData() {
        return signedData;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public int getRequestUserId() {
        return requestUserId;
    }

    @JsonCreator
    public static ChangeAvatarToken of(ObjectNode objectNode) {
        ObjectNode rawData = (ObjectNode) objectNode.get("raw_data");
        return new ChangeAvatarToken(
                objectNode.get("signed_data").asText(),
                rawData.get("target_username").asText(),
                rawData.get("request_user_id").asInt()
        );
    }

    @Override
    public String toString() {
        return "ChangeAvatarToken{" +
                "signedData='" + signedData + '\'' +
                ", targetUsername='" + targetUsername + '\'' +
                ", requestUserId=" + requestUserId +
                '}';
    }
}
