package io.papermc.hangar.service.internal.file;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;

@Service
@ConditionalOnProperty(value = "hangar.storage.type", havingValue = "object")
public class S3FileService implements FileService {
    @Override
    public FileSystemResource getResource(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteDirectory(String dir) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean delete(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] bytes(String path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(InputStream inputStream, String path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(String oldPath, String newPath) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void link(String existingPath, String newPath) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String resolve(String path, String fileName) {
        throw new UnsupportedOperationException();
    }
}
