package io.papermc.hangar.service.internal.uploads;

import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.file.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProjectFiles {

    private final String pluginsDir;
    private final String tmpDir;
    private final FileService fileService;

    @Autowired
    public ProjectFiles(final FileService fileService) {
        this.fileService = fileService;
        this.pluginsDir = fileService.resolve(fileService.getRoot(), "plugins");
        this.tmpDir = fileService.resolve(fileService.getRoot(), "tmp");
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
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void renameProject(final String owner, final String slug, final String newSlug) {
        final String oldProjectDir = this.getProjectDir(owner, slug);
        final String newProjectDir = this.getProjectDir(owner, newSlug);
        try {
            this.fileService.move(oldProjectDir, newProjectDir);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void renameVersion(final String owner, final String slug, final String version, final String newVersionName) {
        final String oldVersionDir = this.getVersionDir(owner, slug, version);
        final String newVersionDir = this.getVersionDir(owner, slug, newVersionName);
        try {
            this.fileService.move(oldVersionDir, newVersionDir);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public String getTempDir(final String owner) {
        return this.fileService.resolve(this.tmpDir, owner);
    }

}
