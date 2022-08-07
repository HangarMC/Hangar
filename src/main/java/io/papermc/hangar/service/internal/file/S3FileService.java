package io.papermc.hangar.service.internal.file;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import io.papermc.hangar.config.hangar.StorageConfig;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

@Service
@ConditionalOnProperty(value = "hangar.storage.type", havingValue = "object")
public class S3FileService implements FileService {

    private final StorageConfig config;
    private final S3Client client;

    public S3FileService(StorageConfig config) throws URISyntaxException {
        this.config = config;
        this.client = S3Client.builder()
            .endpointOverride(new URI(config.getObjectStorageEndpoint()))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(config.getAccessKey(), config.getSecretKey())))
            .region(Region.of("local"))
            .build();
    }

    @Override
    public FileSystemResource getResource(String path) {
        throw new UnsupportedOperationException();// TODO
    }

    @Override
    public boolean exists(String path) {
        client.headObject(HeadObjectRequest.builder().build());
        throw new UnsupportedOperationException();// TODO
    }

    @Override
    public void deleteDirectory(String dir) {
        throw new UnsupportedOperationException();// TODO
    }

    @Override
    public boolean delete(String path) {
        try {
            client.deleteObject(DeleteObjectRequest.builder().bucket(config.getBucket()).key(path).build());
            return true;
        } catch (Exception ex) {
            // TODO does this make remotely sense?
            return false;
        }
    }

    @Override
    public byte[] bytes(String path) throws IOException {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void write(InputStream inputStream, String path) throws IOException {
        throw new UnsupportedOperationException();// TODO
    }

    @Override
    public void move(String oldPath, String newPath) throws IOException {
        throw new UnsupportedOperationException();// TODO
    }

    @Override
    public void link(String existingPath, String newPath) throws IOException {
        throw new UnsupportedOperationException();// TODO
    }

    @Override
    public String resolve(String path, String fileName) {
        if (path.endsWith("/")) {
            return path + fileName;
        } else {
            return path + "/" + fileName;
        }
    }
}
