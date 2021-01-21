package io.papermc.hangar.controllerold.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.config.hangar.HangarConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatusZ {

    private final ObjectNode status;

    private final String BUILD_NUM = "BUILD_NUMBER";
    private final String GIT_BRANCH = "GIT_BRANCH";
    private final String GIT_COMMIT = "GIT_COMMIT";
    private final String JOB_NAME = "JOB_NAME";
    private final String BUILD_TAG = "BUILD_TAG";
    private final String HANGAR_ENV = "HANGAR_ENV";
    private final String SERVICE = "SERVICE";

    @Autowired
    public StatusZ(HangarConfig hangarConfig, ObjectMapper mapper) {
        status = mapper.createObjectNode();
        status.set(BUILD_NUM, mapper.valueToTree(getEnv(BUILD_NUM)));
        status.set(GIT_BRANCH, mapper.valueToTree(getEnv(GIT_BRANCH)));
        status.set(GIT_COMMIT, mapper.valueToTree(getEnv(GIT_COMMIT)));
        status.set(JOB_NAME, mapper.valueToTree(getEnv(JOB_NAME)));
        status.set(BUILD_TAG, mapper.valueToTree(getEnv(BUILD_TAG)));
        status.set(HANGAR_ENV, mapper.valueToTree(getEnv(HANGAR_ENV)));
        status.set(SERVICE, mapper.valueToTree(hangarConfig.getService()));

    }

    public ObjectNode getStatus() {
        return status;
    }

    private String getEnv(String key) {
        return System.getenv().getOrDefault(key, "unknown");
    }
}
