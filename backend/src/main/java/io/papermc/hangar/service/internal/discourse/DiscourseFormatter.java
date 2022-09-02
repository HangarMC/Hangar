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
    public DiscourseFormatter(HangarConfig config) {
        this.config = config;

        try(InputStream resource1 = DiscourseFormatter.class.getResourceAsStream("project_topic.md");
            InputStream resource2 = DiscourseFormatter.class.getResourceAsStream("version_post.md")) {
            if (resource1 == null || resource2 == null) {
                throw new RuntimeException("Error initing discourse formatter, files not found");
            }

            projectTopic = new BufferedReader(new InputStreamReader(resource1)).lines().collect(Collectors.joining("\n"));
            versionRelease = new BufferedReader(new InputStreamReader(resource2)).lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            projectTopic = "ERROR";
            versionRelease = "ERROR";
            e.printStackTrace();
        }
    }

    public String formatProjectTitle(ProjectTable project) {
        return project.getName() + (project.getDescription() != null && !project.getDescription().isBlank() ? " - " + project.getDescription() : "");
    }

    public String formatProjectTopic(ProjectTable project, String content) {
        return String.format(projectTopic, project.getName(), namespaceToUrl(project), content);
    }

    public String formatVersionRelease(ProjectTable project, ProjectVersionTable version, String content) {
        return String.format(versionRelease, project.getName(), namespaceToUrl(project), versionToUrl(project, version), content == null ? "*No description given*" : content);
    }

    private String namespaceToUrl(ProjectTable project) {
        return base() + "/" + project.getOwnerName() + "/" + project.getSlug();
    }

    private String versionToUrl(ProjectTable project, ProjectVersionTable version) {
        return namespaceToUrl(project) + "/versions/" +  version.getName() + "/";
    }

    private String base() {
        return config.getBaseUrl();
    }
}
