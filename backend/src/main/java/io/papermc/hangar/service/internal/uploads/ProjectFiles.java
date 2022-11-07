package io.papermc.hangar.service.internal.uploads;

import io.papermc.hangar.config.hangar.StorageConfig;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.file.FileService;
import io.papermc.hangar.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ProjectFiles {

    private static final Logger logger = LoggerFactory.getLogger(ProjectFiles.class);

    private final String pluginsDir;
    private final Path tmpDir;
    private final FileService fileService;

    @Autowired
    public ProjectFiles(StorageConfig storageConfig, FileService fileService) {
        this.fileService = fileService;
        Path uploadsDir = Path.of(storageConfig.workDir());
        pluginsDir = fileService.resolve(fileService.getRoot(), "plugins");
        tmpDir = uploadsDir.resolve("tmp");
        if (Files.exists(tmpDir)) {
            FileUtils.deleteDirectory(tmpDir);
        }
        logger.info("Cleaned up tmp files and inited work dir {} ", uploadsDir);
    }

    public String getProjectDir(String owner, String name) {
        return fileService.resolve(getUserDir(owner), name);
    }

    public String getVersionDir(String owner, String name, String version) {
        return fileService.resolve(fileService.resolve(getProjectDir(owner, name), "versions"), version);
    }

    public String getVersionDir(String owner, String name, String version, Platform platform) {
        return fileService.resolve(getVersionDir(owner, name, version), platform.name());
    }

    public String getVersionDir(String owner, String name, String version, Platform platform, String fileName) {
        return fileService.resolve(getVersionDir(owner, name, version, platform), fileName);
    }

    public String getUserDir(String user) {
        return fileService.resolve(pluginsDir, user);
    }

    public void transferProject(String owner, String newOwner, String slug) {
        final String oldProjectDir = getProjectDir(owner, slug);
        final String newProjectDir = getProjectDir(newOwner, slug);
        try {
            fileService.move(oldProjectDir, newProjectDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void renameProject(String owner, String slug, String newSlug) {
        final String oldProjectDir = getProjectDir(owner, slug);
        final String newProjectDir = getProjectDir(owner, newSlug);
        try {
            fileService.move(oldProjectDir, newProjectDir);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void renameVersion(String owner, String slug, String version, String newVersionName) {
        final String oldVersionDir = getVersionDir(owner, slug, version);
        final String newVersionDir = getVersionDir(owner, slug, newVersionName);
        try {
            fileService.move(oldVersionDir, newVersionDir);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public String getIconsDir(String owner, String name) {
        return fileService.resolve(getProjectDir(owner, name), "icons");
    }

    public String getIconPath(String owner, String name) {
        return fileService.resolve(getIconsDir(owner, name), "icon.png");
    }

    public Path getTempDir(String owner) {
        return tmpDir.resolve(owner);
    }

}
