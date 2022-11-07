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

    public S3FileService(StorageConfig config, ResourceLoader resourceLoader, S3Template s3Template) {
        this.config = config;
        this.resourceLoader = resourceLoader;
        this.s3Template = s3Template;
    }

    @Override
    public Resource getResource(String path) {
        return this.resourceLoader.getResource(path);
    }

    @Override
    public boolean exists(String path) {
        return getResource(path).exists();
    }

    @Override
    public void deleteDirectory(String dir) {
        this.s3Template.deleteObject(config.bucket(), dir);
    }

    @Override
    public boolean delete(String path) {
        this.s3Template.deleteObject(path);
        return true;
    }

    @Override
    public byte[] bytes(String path) throws IOException {
        return getResource(path).getInputStream().readAllBytes();
    }

    @Override
    public void write(InputStream inputStream, String path) throws IOException {
        try (OutputStream outputStream = ((S3Resource) getResource(path)).getOutputStream()) {
            outputStream.write(inputStream.readAllBytes());
        }
    }

    @Override
    public void move(String oldPath, String newPath) throws IOException {
        if (!oldPath.startsWith(getRoot()) && newPath.startsWith(getRoot())) {
            write(Files.newInputStream(Path.of(oldPath)), newPath);
        } else {
            throw new UnsupportedOperationException("cant move " + oldPath + " to " + newPath);// TODO
        }
    }

    @Override
    public void link(String existingPath, String newPath) throws IOException {
        throw new UnsupportedOperationException("cant move " + existingPath + " to " + newPath);// TODO
    }

    @Override
    public String resolve(String path, String fileName) {
        if (path.endsWith("/")) {
            return path + fileName;
        } else {
            return path + "/" + fileName;
        }
    }

    @Override
    public String getRoot() {
        return "s3://" + config.bucket() + "/";
    }

    @Override
    public String getDownloadUrl(String user, String project, String version, Platform platform, String fileName) {
        return config.cdnEndpoint() + (config.cdnIncludeBucket() ? "/" + config.bucket() : "") + "/plugins/" + user + "/" + project + "/versions/" + version + "/" + platform.name() + "/" + fileName;
    }
}
