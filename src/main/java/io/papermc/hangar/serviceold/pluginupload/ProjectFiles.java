package io.papermc.hangar.serviceold.pluginupload;

import io.papermc.hangar.config.hangar.HangarConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        logger.info("Init work dir {} ", uploadsDir);
    }

    public Path getProjectDir(String owner, String name) {
        return getUserDir(owner).resolve(name);
    }

    public Path getVersionDir(String owner, String name, String version) {
        return getProjectDir(owner, name).resolve("versions").resolve(version);
    }

    public Path getUserDir(String user) {
        return pluginsDir.resolve(user);
    }

    public void renameProject(String owner, String oldName, String newName) {
        Path newProjectDir = getProjectDir(owner, newName);
        Path oldProjectDir = getProjectDir(owner, oldName);

        try {
            Files.move(oldProjectDir, newProjectDir);
        } catch (IOException e) {
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

    public Path getPendingIconDir(String owner, String name) {
        return getIconDir(owner, name).resolve("pending");
    }

    public Path getPendingIconPath(String owner, String name) {
        return findFirstFile(getPendingIconDir(owner, name));
    }

    public Path getTempDir(String owner) {
        return tmpDir.resolve(owner);
    }

    private Path findFirstFile(Path dir) {
        if (Files.exists(dir)) {
            try (Stream<Path> pathStream = Files.list(dir)) {
                return pathStream.filter(Predicate.not(Files::isDirectory)).findFirst().orElse(null);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else return null;
    }
}
