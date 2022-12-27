package io.papermc.hangar.service.internal.file;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import io.papermc.hangar.config.hangar.StorageConfig;
import io.papermc.hangar.model.common.Platform;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "hangar.storage.type", havingValue = "object")
public class S3FileService implements FileService {

    private final StorageConfig config;
    private final ResourceLoader resourceLoader;
    private final S3Template s3Template;

    public S3FileService(final StorageConfig config, final ResourceLoader resourceLoader, final S3Template s3Template) {
        this.config = config;
        this.resourceLoader = resourceLoader;
        this.s3Template = s3Template;
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
    public void deleteDirectory(final String dir) {
        this.s3Template.deleteObject(this.config.bucket(), dir);
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
            this.write(Files.newInputStream(Path.of(oldPath)), newPath);
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
        return this.config.cdnEndpoint() + (this.config.cdnIncludeBucket() ? "/" + this.config.bucket() : "") + "/plugins/" + user + "/" + project + "/versions/" + version + "/" + platform.name() + "/" + fileName;
    }
}
