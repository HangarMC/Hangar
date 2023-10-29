package io.papermc.hangar.service.internal.file;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.config.hangar.StorageConfig;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.util.FileUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "hangar.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageFileService implements FileService {

    private final StorageConfig config;
    private final HangarConfig hangarConfig;

    public LocalStorageFileService(final StorageConfig config, final HangarConfig hangarConfig) {
        this.config = config;
        this.hangarConfig = hangarConfig;
    }

    @Override
    public Resource getResource(final String path) {
        return new FileSystemResource(path);
    }

    @Override
    public boolean exists(final String path) {
        return Files.exists(Path.of(path));
    }

    @Override
    public void deleteDirectory(final String dir) {
        FileUtils.deleteDirectory(Path.of(dir));
    }

    @Override
    public boolean delete(final String path) {
        return FileUtils.delete(Path.of(path));
    }

    @Override
    public byte[] bytes(final String path) throws IOException {
        return Files.readAllBytes(Path.of(path));
    }

    @Override
    public void write(final InputStream inputStream, final String path, final @Nullable String contentType) throws IOException {
        final Path p = Path.of(path);
        if (Files.notExists(p)) {
            Files.createDirectories(p.getParent());
        }
        Files.copy(inputStream, p, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void move(final String oldPathString, final String newPathString) throws IOException {
        final Path oldPath = Path.of(oldPathString);
        final Path newPath = Path.of(newPathString);
        if (Files.notExists(newPath)) {
            Files.createDirectories(newPath.getParent());
        }

        if (Files.exists(oldPath)) {
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            if (Files.notExists(newPath)) {
                throw new IOException("Didn't successfully move");
            }
        }
    }

    @Override
    public void link(final String existingPathString, final String newPathString) throws IOException {
        final Path existingPath = Path.of(existingPathString);
        final Path newPath = Path.of(newPathString);
        if (Files.notExists(newPath)) {
            Files.createDirectories(newPath.getParent());
        }
        Files.createLink(newPath, existingPath);
    }

    @Override
    public String resolve(final String path, final String fileName) {
        return Path.of(path).resolve(fileName).toString();
    }

    @Override
    public String getRoot() {
        return this.config.workDir();
    }

    @Override
    public String getVersionDownloadUrl(final String user, final String project, final String version, final Platform platform, final String fileName) {
        return this.hangarConfig.getBaseUrl() + "/api/v1/projects/" + user + "/" + project + "/versions/" + version + "/" + platform.name() + "/download";
    }

    @Override
    public String getAvatarUrl(final String type, final String subject, final String version) {
        return this.hangarConfig.getBaseUrl() + "/api/internal/avatar/" + type + "/" + subject + ".webp?v=" + version;
    }
}
