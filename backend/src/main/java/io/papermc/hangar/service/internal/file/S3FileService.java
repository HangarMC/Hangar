package io.papermc.hangar.service.internal.file;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import io.papermc.hangar.config.hangar.StorageConfig;
import io.papermc.hangar.model.common.Platform;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

@Service
@ConditionalOnProperty(value = "hangar.storage.type", havingValue = "object")
public class S3FileService implements FileService {

    private static final String URL_FORMAT = "%s/plugins/%s/%s/versions/%s/%s/%s";
    private final StorageConfig config;
    private final ResourceLoader resourceLoader;
    private final S3Template s3Template;
    private final S3Client s3Client;

    public S3FileService(final StorageConfig config, final ResourceLoader resourceLoader, final S3Template s3Template, final S3Client s3Client) {
        this.config = config;
        this.resourceLoader = resourceLoader;
        this.s3Template = s3Template;
        this.s3Client = s3Client;
    }

    @Override
    public Resource getResource(final String path) {
        return this.resourceLoader.getResource(path);
    }

    @Override
    public boolean exists(final String path) {
        return this.getResource(path).exists();
    }

    @Override
    public void deleteDirectory(final String path) {
        this.s3Template.deleteObject(path);
    }

    @Override
    public boolean delete(final String path) {
        this.s3Template.deleteObject(path);
        return true;
    }

    @Override
    public byte[] bytes(final String path) throws IOException {
        return this.getResource(path).getInputStream().readAllBytes();
    }

    @Override
    public void write(final InputStream inputStream, final String path) throws IOException {
        try (final OutputStream outputStream = ((S3Resource) this.getResource(path)).getOutputStream()) {
            outputStream.write(inputStream.readAllBytes());
        }
    }

    @Override
    public void move(final String oldPath, final String newPath) throws IOException {
        if (!oldPath.startsWith(this.getRoot()) && newPath.startsWith(this.getRoot())) {
            // upload from file to s3
            this.write(Files.newInputStream(Path.of(oldPath)), newPath);
        } else if (oldPath.startsWith(this.getRoot()) && newPath.startsWith(this.getRoot())) {
            // "rename" in s3
            this.s3Client.copyObject((builder -> builder
                .sourceBucket(this.config.bucket()).sourceKey(oldPath.replace(this.getRoot(), ""))
                .destinationBucket(this.config.bucket()).destinationKey(newPath.replace(this.getRoot(), ""))
            ));
            this.s3Template.deleteObject(oldPath);
        } else {
            throw new UnsupportedOperationException("cant move " + oldPath + " to " + newPath);
        }
    }

    @Override
    public void link(final String existingPath, final String newPath) throws IOException {
        throw new UnsupportedOperationException("cant link " + existingPath + " to " + newPath);
    }

    @Override
    public String resolve(final String path, final String fileName) {
        if (path.endsWith("/")) {
            return path + fileName;
        } else {
            return path + "/" + fileName;
        }
    }

    @Override
    public String getRoot() {
        return "s3://" + this.config.bucket() + "/";
    }

    @Override
    public String getDownloadUrl(final String user, final String project, final String version, final Platform platform, final String fileName) {
        final String encodedVersion = URLEncoder.encode(version, StandardCharsets.UTF_8);
        final String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        final String endpoint = this.config.cdnIncludeBucket() ? this.config.cdnEndpoint() + "/" + this.config.bucket() : this.config.cdnEndpoint();
        return URL_FORMAT.formatted(endpoint, user, project, encodedVersion, platform.name(), encodedFileName);
    }
}
