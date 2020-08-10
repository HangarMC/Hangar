package me.minidigger.hangar.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FileUtils {

    private FileUtils() { }

    public static void deletedFiles(Path superDir) {
        try (Stream<Path> files = Files.list(superDir)) {
            files.filter(Predicate.not(Files::isDirectory)).forEach(FileUtils::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void delete(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) { }
    }
}
