package io.papermc.hangar.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
    }

    public static void deletedFiles(final Path superDir) {
        try (final Stream<Path> files = Files.list(superDir)) {
            files.filter(Predicate.not(Files::isDirectory)).forEach(FileUtils::delete);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean delete(final Path path) {
        if (path == null) return false;
        try {
            return Files.deleteIfExists(path);
        } catch (final IOException ignored) {
            return false;
        }
    }

    public static void deleteDirectory(final Path dir) {
        if (Files.exists(dir)) {
            try {
                Files.walkFileTree(dir, new DeleteFileVisitor());
            } catch (final IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.debug("Tried to clean directory that doesn't exist: {}", dir);
        }
    }

    private static class DeleteFileVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            if (Files.exists(file)) {
                Files.delete(file);
            } else {
                logger.debug("Tried to remove file that doesn't exist: {}", file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            if (Files.exists(dir)) {
                Files.delete(dir);
            } else {
                logger.debug("Tried to remove directory that doesn't exist: {}", dir);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
