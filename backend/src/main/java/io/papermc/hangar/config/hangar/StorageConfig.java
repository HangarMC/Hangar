package io.papermc.hangar.config.hangar;

import io.awspring.cloud.autoconfigure.core.AwsProperties;
import io.papermc.hangar.HangarApplication;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.URISyntaxException;

@ConfigurationProperties(prefix = "hangar.storage")
public record StorageConfig(
    @DefaultValue("local") String type,
    @DefaultValue("backend/work") String workDir,
    String accessKey,
    String secretKey,
    String bucket,
    String objectStorageEndpoint,
    String cdnEndpoint,
    @DefaultValue("true") boolean cdnIncludeBucket
) {

    @Component
    public record AWSConfig(StorageConfig storageConfig) {

        @Bean
        public StaticCredentialsProvider credProvider() {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(this.storageConfig.accessKey(), this.storageConfig.secretKey()));
        }

        @Bean
        public AwsRegionProvider regionProvider() {
            return () -> Region.of("hangar");
        }

        @Bean
        public AwsProperties awsProperties() throws URISyntaxException {
            final AwsProperties awsProperties = new AwsProperties();
            awsProperties.setEndpoint(new URI(this.storageConfig.objectStorageEndpoint()));
            return awsProperties;
        }

    }
}
