package io.papermc.hangar.service.internal.discourse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

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

        try {
            URL resource1 = DiscourseFormatter.class.getResource("project_topic.md");
            URL resource2 = DiscourseFormatter.class.getResource("version_post.md");

            if (resource1 == null || resource2 == null) {
                throw new RuntimeException("Error initing discourse formatter, files not found");
            }

            projectTopic = Files.readString(Paths.get(resource1.toURI()));
            versionRelease = Files.readString(Paths.get(resource2.toURI()));
        } catch (Exception e) {
            // TODO better handle this, it seems like staging doesn't have the files or smth, idk
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
