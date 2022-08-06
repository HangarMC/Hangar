package io.papermc.hangar.service.internal.file;

import org.springframework.core.io.FileSystemResource;
import java.io.IOException;
import java.io.InputStream;

public interface FileService {

    FileSystemResource getResource(String path);

    boolean exists(String path);

    void deleteDirectory(String dir);

    boolean delete(String path);

    byte[] bytes(String path) throws IOException;

    void write(InputStream inputStream, String path) throws IOException;

    void move(String oldPath, String newPath) throws IOException;

    void link(String existingPath, String newPath) throws IOException;

    String resolve(String path, String fileName);
}
