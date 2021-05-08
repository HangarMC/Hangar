package io.papermc.hangar.service.internal.discourse;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.papermc.hangar.model.api.project.ProjectNamespace;
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.model.internal.versions.HangarVersion;

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

    public static String formatProjectTitle(HangarProject project) {
        return project.getName() + (project.getDescription() != null && !project.getDescription().isBlank() ? " - " + project.getDescription() : "");
    }

    public static String formatProjectTopic(HangarProject project, String content) {
        return String.format(PROJECT_TOPIC, project.getName(), namespaceToUrl(project.getNamespace()), content);
    }

    public static String formatVersionRelease(HangarProject project, HangarVersion version, String content) {
        return String.format(VERSION_RELEASE, project.getName(), namespaceToUrl(project.getNamespace()), versionToUrl(project, version), content == null ? "*No description given*" : content);
    }

    private static String namespaceToUrl(ProjectNamespace namespace) {
        return base() + "/" + namespace.getOwner() + "/" + namespace.getSlug();
    }

    private static String versionToUrl(HangarProject project, HangarVersion version) {
        return namespaceToUrl(project.getNamespace()) + "/versions/" +  version.getName();
    }

    private static String base() {
        return ""; // TODO get base url here
    }
}
