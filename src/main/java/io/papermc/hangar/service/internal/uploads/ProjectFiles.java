package io.papermc.hangar.service.internal.uploads;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
public class ProjectFiles {

    private static final Logger logger = LoggerFactory.getLogger(ProjectFiles.class);

    private final Path pluginsDir;
    private final Path tmpDir;

    @Autowired
    public ProjectFiles(HangarConfig hangarConfig) {
        Path uploadsDir = Path.of(hangarConfig.getPluginUploadDir());
        pluginsDir = uploadsDir.resolve("plugins");
        tmpDir = uploadsDir.resolve("tmp");
        if (Files.exists(tmpDir)) {
            FileUtils.deleteDirectory(tmpDir);
        }
        logger.info("Cleaned up tmp files and inited work dir {} ", uploadsDir);
    }

    public Path getProjectDir(String owner, String name) {
        return getUserDir(owner).resolve(name);
    }

    public Path getVersionDir(String owner, String name, String version) {
        return getProjectDir(owner, name).resolve("versions").resolve(version);
    }

    public Path getVersionDir(String owner, String name, String version, Platform platform) {
        return getVersionDir(owner, name, version).resolve(platform.name());
    }

    public Path getUserDir(String user) {
        return pluginsDir.resolve(user);
    }

    public void transferProject(String owner, String newOwner, String slug) {
        final Path oldProjectDir = getProjectDir(owner, slug);
        if (!Files.exists(oldProjectDir)) {
            return;
        }

        final Path newProjectDir = getProjectDir(newOwner, slug);
        try {
            Files.createDirectories(newProjectDir);
            Files.move(oldProjectDir, newProjectDir, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void renameProject(String owner, String slug, String newSlug) {
        final Path oldProjectDir = getProjectDir(owner, slug);
        if (!Files.exists(oldProjectDir)) {
            return;
        }

        final Path newProjectDir = getProjectDir(owner, newSlug);
        try {
            Files.createDirectories(newProjectDir);
            Files.move(oldProjectDir, newProjectDir, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void renameVersion(String owner, String slug, String version, String newVersionName) {
        final Path oldVersionDir = getVersionDir(owner, slug, version);
        if (!Files.exists(oldVersionDir)) {
            return;
        }

        final Path newVersionDir = getVersionDir(owner, slug, newVersionName);
        try {
            Files.move(oldVersionDir, newVersionDir, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public Path getIconsDir(String owner, String name) {
        return getProjectDir(owner, name).resolve("icons");
    }

    public Path getIconDir(String owner, String name) {
        return getIconsDir(owner, name).resolve("icon");
    }

    public Path getIconPath(String owner, String name) {
        return findFirstFile(getIconDir(owner, name));
    }

    public Path getTempDir(String owner) {
        return tmpDir.resolve(owner);
    }

    private Path findFirstFile(Path dir) {
        if (!Files.exists(dir)) {
            return null;
        }

        try (Stream<Path> pathStream = Files.list(dir)) {
            return pathStream.filter(Predicate.not(Files::isDirectory)).findFirst().orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
