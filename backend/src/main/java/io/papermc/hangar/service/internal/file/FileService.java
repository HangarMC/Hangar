package io.papermc.hangar.service.internal.file;

import io.papermc.hangar.model.common.Platform;
import java.io.IOException;
import java.io.InputStream;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.Resource;

public interface FileService {

    Resource getResource(String path);

    boolean exists(String path);

    void deleteDirectory(String dir);

    boolean delete(String path);

    byte[] bytes(String path) throws IOException;

    void write(InputStream inputStream, String path, @Nullable String contentType) throws IOException;

    default void moveFile(String oldPath, String newPath) throws IOException {
        this.move(oldPath, newPath);
    }

    void move(String oldPath, String newPath) throws IOException;

    void link(String existingPath, String newPath) throws IOException;

    String resolve(String path, String fileName);

    String getRoot();

    String getVersionDownloadUrl(String user, String project, String version, Platform platform, String fileName);

    String getAvatarUrl(String type, String subject, String version);
}
