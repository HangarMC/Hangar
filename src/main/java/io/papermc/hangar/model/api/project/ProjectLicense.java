package io.papermc.hangar.model.api.project;

public class ProjectLicense {

    private final String name;
    private final String url;

    public ProjectLicense(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
