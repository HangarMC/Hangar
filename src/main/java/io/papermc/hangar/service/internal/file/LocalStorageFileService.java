package io.papermc.hangar.service.internal.file;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import io.papermc.hangar.util.FileUtils;

@Service
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
        Path p = Path.of(path);
        if (Files.notExists(p)) {
            Files.createDirectories(p.getParent());
        }
        Files.copy(inputStream, p);
    }

    @Override
    public void move(String oldPathString, String newPathString) throws IOException {
        Path oldPath = Path.of(oldPathString);
        Path newPath = Path.of(newPathString);
        if (Files.notExists(newPath)) {
            Files.createDirectories(newPath.getParent());
        }

        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        if (Files.notExists(newPath)) {
            throw new IOException("Didn't successfully move");
        }
    }

    @Override
    public void link(String existingPathString, String newPathString) throws IOException {
        Path existingPath = Path.of(existingPathString);
        Path newPath = Path.of(newPathString);
        if (Files.notExists(newPath)) {
            Files.createDirectories(newPath.getParent());
        }
        Files.createLink(newPath, existingPath);
    }

    @Override
    public String resolve(String path, String fileName) {
        return Path.of(path).resolve(fileName).toString();
    }
}
