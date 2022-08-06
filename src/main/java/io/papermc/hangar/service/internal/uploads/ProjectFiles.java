package io.papermc.hangar.service.internal.uploads;

import io.papermc.hangar.config.hangar.HangarConfig;
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
import java.nio.file.StandardCopyOption;

@Component
public class ProjectFiles {

    private static final Logger logger = LoggerFactory.getLogger(ProjectFiles.class);

    private final Path pluginsDir;
    private final Path tmpDir;
    private final FileService fileService;

    @Autowired
    public ProjectFiles(HangarConfig hangarConfig, FileService fileService) {
        this.fileService = fileService;
        Path uploadsDir = Path.of(hangarConfig.getPluginUploadDir());
        pluginsDir = uploadsDir.resolve("plugins");
        tmpDir = uploadsDir.resolve("tmp");
        if (Files.exists(tmpDir)) {
            FileUtils.deleteDirectory(tmpDir);
        }
        logger.info("Cleaned up tmp files and inited work dir {} ", uploadsDir);
    }

    public String getProjectDir(String owner, String name) {
        return getUserDir(owner) + "/" + name;
    }

    public String getVersionDir(String owner, String name, String version) {
        return getProjectDir(owner, name) + "/versions/" + version;
    }

    public String getVersionDir(String owner, String name, String version, Platform platform) {
        return getVersionDir(owner, name, version) + "/" + platform.name();
    }

    public String getVersionDir(String owner, String name, String version, Platform platform, String fileName) {
        return getVersionDir(owner,name, version, platform) + "/" + fileName;
    }

    public String getUserDir(String user) {
        return pluginsDir + "/" + user;
    }

    public void transferProject(String owner, String newOwner, String slug) {
        final String oldProjectDir = getProjectDir(owner, slug);
        if (!Files.exists(oldProjectDir)) {
            return;
        }

        final String newProjectDir = getProjectDir(newOwner, slug);
        try {
            fileService.move(oldProjectDir, newProjectDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void renameProject(String owner, String slug, String newSlug) {
        final String oldProjectDir = getProjectDir(owner, slug);
        if (!Files.exists(oldProjectDir)) {
            return;
        }

        final String newProjectDir = getProjectDir(owner, newSlug);
        try {
            fileService.move(oldProjectDir, newProjectDir);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void renameVersion(String owner, String slug, String version, String newVersionName) {
        final String oldVersionDir = getVersionDir(owner, slug, version);
        if (!Files.exists(oldVersionDir)) {
            return;
        }

        final String newVersionDir = getVersionDir(owner, slug, newVersionName);
        try {
            fileService.move(oldVersionDir, newVersionDir);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public String getIconsDir(String owner, String name) {
        return getProjectDir(owner, name) + "/icons";
    }

    public String getIconPath(String owner, String name) {
        return getIconsDir(owner, name) + "/icon.png";
    }

    public Path getTempDir(String owner) {
        return tmpDir.resolve(owner);
    }

}
