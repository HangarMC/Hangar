package io.papermc.hangar.service.internal.file;

import org.springframework.core.io.FileSystemResource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import io.papermc.hangar.util.FileUtils;

public class LocalStorageFileService implements FileService {
    @Override
    public FileSystemResource getResource(String path) {
        return new FileSystemResource(path);
    }

    @Override
    public boolean exists(String path) {
        return Files.exists(Path.of(path));
    }

    @Override
    public void deleteDirectory(String dir) {
        FileUtils.deleteDirectory(Path.of(dir));
    }

    @Override
    public boolean delete(String path) {
        return FileUtils.delete(Path.of(path));
    }

    @Override
    public byte[] bytes(String path) throws IOException {
        return Files.readAllBytes(Path.of(path));
    }

    @Override
    public void write(InputStream inputStream, String path) throws IOException {
        Files.copy(inputStream, Path.of(path));
    }
}
