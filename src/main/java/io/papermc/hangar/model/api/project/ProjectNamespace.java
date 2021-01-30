package io.papermc.hangar.model.api.project;

public class ProjectNamespace {

    private final String owner;
    private final String slug;

    public ProjectNamespace(String owner, String slug) {
        this.owner = owner;
        this.slug = slug;
    }

    public String getOwner() {
        return owner;
    }

    public String getSlug() {
        return slug;
    }

    @Override
    public String toString() {
        return "ProjectNamespace{" +
                "owner='" + owner + '\'' +
                ", slug='" + slug + '\'' +
                '}';
    }
}

