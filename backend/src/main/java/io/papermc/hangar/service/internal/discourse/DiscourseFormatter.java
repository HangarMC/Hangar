package io.papermc.hangar.service.internal.discourse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;

@Component
public class DiscourseFormatter {

    private final HangarConfig config;

    private String projectTopic;
    private String versionRelease;

    @Autowired
    public DiscourseFormatter(final HangarConfig config) {
        this.config = config;

        try(final InputStream resource1 = DiscourseFormatter.class.getResourceAsStream("project_topic.md");
            final InputStream resource2 = DiscourseFormatter.class.getResourceAsStream("version_post.md")) {
            if (resource1 == null || resource2 == null) {
                throw new RuntimeException("Error initing discourse formatter, files not found");
            }

            this.projectTopic = new BufferedReader(new InputStreamReader(resource1)).lines().collect(Collectors.joining("\n"));
            this.versionRelease = new BufferedReader(new InputStreamReader(resource2)).lines().collect(Collectors.joining("\n"));
        } catch (final Exception e) {
            this.projectTopic = "ERROR";
            this.versionRelease = "ERROR";
            e.printStackTrace();
        }
    }

    public String formatProjectTitle(final ProjectTable project) {
        return project.getName() + (project.getDescription() != null && !project.getDescription().isBlank() ? " - " + project.getDescription() : "");
    }

    public String formatProjectTopic(final ProjectTable project, final String content) {
        return String.format(this.projectTopic, project.getName(), this.namespaceToUrl(project), content);
    }

    public String formatVersionRelease(final ProjectTable project, final ProjectVersionTable version, final String content) {
        return String.format(this.versionRelease, project.getName(), this.namespaceToUrl(project), this.versionToUrl(project, version), content == null ? "*No description given*" : content);
    }

    private String namespaceToUrl(final ProjectTable project) {
        return this.base() + "/" + project.getOwnerName() + "/" + project.getSlug();
    }

    private String versionToUrl(final ProjectTable project, final ProjectVersionTable version) {
        return this.namespaceToUrl(project) + "/versions/" +  version.getName() + "/";
    }

    private String base() {
        return this.config.getBaseUrl();
    }
}
