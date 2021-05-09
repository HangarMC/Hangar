package io.papermc.hangar.service.internal.discourse;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;

public class DiscourseFormatter {

    private static final String PROJECT_TOPIC;
    private static final String VERSION_RELEASE;

    static {
        try {
            URL resource1 = DiscourseFormatter.class.getResource("project_topic.md");
            URL resource2 = DiscourseFormatter.class.getResource("version_post.md");

            if (resource1 == null || resource2 == null) {
                throw new RuntimeException("Error initing discourse formatter, files not found");
            }

            PROJECT_TOPIC = Files.readString(Paths.get(resource1.toURI()));
            VERSION_RELEASE = Files.readString(Paths.get(resource2.toURI()));
        } catch (Exception e) {
            throw new RuntimeException("Error initing discourse formatter");
        }
    }

    public static String formatProjectTitle(ProjectTable project) {
        return project.getName() + (project.getDescription() != null && !project.getDescription().isBlank() ? " - " + project.getDescription() : "");
    }

    public static String formatProjectTopic(ProjectTable project, String content) {
        return String.format(PROJECT_TOPIC, project.getName(), namespaceToUrl(project), content);
    }

    public static String formatVersionRelease(ProjectTable project, ProjectVersionTable version, String content) {
        return String.format(VERSION_RELEASE, project.getName(), namespaceToUrl(project), versionToUrl(project, version), content == null ? "*No description given*" : content);
    }

    private static String namespaceToUrl(ProjectTable project) {
        return base() + "/" + project.getOwnerName() + "/" + project.getSlug();
    }

    private static String versionToUrl(ProjectTable project, ProjectVersionTable version) {
        return namespaceToUrl(project) + "/versions/" +  version.getName();
    }

    private static String base() {
        return ""; // TODO get base url here
    }
}
