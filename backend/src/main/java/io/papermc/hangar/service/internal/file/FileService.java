package io.papermc.hangar.service.internal.file;

import io.papermc.hangar.model.common.Platform;
import java.io.IOException;
import java.io.InputStream;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    Resource getResource(String path);

    boolean exists(String path);

    void deleteDirectory(String dir);

    boolean delete(String path);

    byte[] bytes(String path) throws IOException;

    void write(InputStream inputStream, String path, @Nullable String contentType) throws IOException;

    void write(MultipartFile file, byte[] fileBytes, String path, @Nullable String contentType) throws IOException;

    default void moveFile(String oldPath, String newPath) throws IOException {
        this.move(oldPath, newPath);
    }

    void move(String oldPath, String newPath) throws IOException;

    void link(String existingPath, String newPath) throws IOException;

    String resolve(String path, String fileName);

    String getRoot();

    String getVersionDownloadUrl(String user, String project, String version, Platform platform, String fileName);

    default String getAvatarUrl(String type, String subject, String version) {
        return getAvatarUrlPrefix() + "/" + type + "/" + subject + ".webp?v=" + version;
    }

    String getAvatarUrlPrefix();
}
