package io.papermc.hangar.config.hangar;

import io.papermc.hangar.HangarApplication;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

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

    @Bean
    public StaticCredentialsProvider credProvider() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(getAccessKey(), getSecretKey()));
    }

    @Bean
    public AwsRegionProvider regionProvider() {
        return () -> Region.of("hangar");
    }

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
