package io.papermc.hangar.service.internal.uploads;

import io.papermc.hangar.config.hangar.StorageConfig;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.file.FileService;
import io.papermc.hangar.util.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProjectFiles {

    private static final Logger logger = LoggerFactory.getLogger(ProjectFiles.class);

    private final String pluginsDir;
    private final Path tmpDir;
    private final FileService fileService;

    @Autowired
    public ProjectFiles(final StorageConfig storageConfig, final FileService fileService) {
        this.fileService = fileService;
        final Path uploadsDir = Path.of(storageConfig.workDir());
        this.pluginsDir = fileService.resolve(fileService.getRoot(), "plugins");
        this.tmpDir = uploadsDir.resolve("tmp");
        if (Files.exists(this.tmpDir)) {
            FileUtils.deleteDirectory(this.tmpDir);
        }
        logger.info("Cleaned up tmp files and inited work dir {} ", uploadsDir);
    }

    public String getProjectDir(final String owner, final String slug) {
        return this.fileService.resolve(this.getUserDir(owner), slug);
    }

    public String getVersionDir(final String owner, final String slug, final String version) {
        return this.fileService.resolve(this.fileService.resolve(this.getProjectDir(owner, slug), "versions"), version);
    }

    public String getVersionDir(final String owner, final String slug, final String version, final Platform platform) {
        return this.fileService.resolve(this.getVersionDir(owner, slug, version), platform.name());
    }

    public String getVersionDir(final String owner, final String name, final String version, final Platform platform, final String fileName) {
        return this.fileService.resolve(this.getVersionDir(owner, name, version, platform), fileName);
    }

    public String getUserDir(final String user) {
        return this.fileService.resolve(this.pluginsDir, user);
    }

    public void transferProject(final String owner, final String newOwner, final String slug) {
        final String oldProjectDir = this.getProjectDir(owner, slug);
        final String newProjectDir = this.getProjectDir(newOwner, slug);
        try {
            this.fileService.move(oldProjectDir, newProjectDir);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void renameProject(final String owner, final String slug, final String newSlug) {
        final String oldProjectDir = this.getProjectDir(owner, slug);
        final String newProjectDir = this.getProjectDir(owner, newSlug);
        try {
            this.fileService.move(oldProjectDir, newProjectDir);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void renameVersion(final String owner, final String slug, final String version, final String newVersionName) {
        final String oldVersionDir = this.getVersionDir(owner, slug, version);
        final String newVersionDir = this.getVersionDir(owner, slug, newVersionName);
        try {
            this.fileService.move(oldVersionDir, newVersionDir);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public String getIconsDir(final String owner, final String slug) {
        return this.fileService.resolve(this.getProjectDir(owner, slug), "icons");
    }

    public String getIconPath(final String owner, final String slug) {
        return this.fileService.resolve(this.getIconsDir(owner, slug), "icon.png");
    }

    public Path getTempDir(final String owner) {
        return this.tmpDir.resolve(owner);
    }

}
