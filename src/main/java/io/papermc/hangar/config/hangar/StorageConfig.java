package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;
import io.papermc.hangar.HangarApplication;

@Component
@ConfigurationProperties(prefix = "hangar.storage")
public class StorageConfig {

    // type = local or object
    private String type = "local";
    // local
    private String pluginUploadDir = new ApplicationHome(HangarApplication.class).getDir().toPath().resolve("work").toString();
    // object
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String objectStorageEndpoint;

    public String getPluginUploadDir() {
        return pluginUploadDir;
    }

    public void setPluginUploadDir(String pluginUploadDir) {
        this.pluginUploadDir = pluginUploadDir;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getObjectStorageEndpoint() {
        return objectStorageEndpoint;
    }

    public void setObjectStorageEndpoint(String objectStorageEndpoint) {
        this.objectStorageEndpoint = objectStorageEndpoint;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
}
