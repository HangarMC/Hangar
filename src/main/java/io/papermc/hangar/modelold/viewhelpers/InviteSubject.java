package io.papermc.hangar.modelold.viewhelpers;

import io.papermc.hangar.db.modelold.OrganizationsTable;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.model.Visitable;

public class InviteSubject<T extends Visitable> {

    public static final InviteSubject<ProjectsTable> PROJECT = new InviteSubject("project");
    public static final InviteSubject<OrganizationsTable> ORGANIZATION = new InviteSubject("organization");

    private final String type;
    private final String name;
    private final String url;

    private InviteSubject(String type) {
        this.type = type;
        this.name = null;
        this.url = null;
    }

    private InviteSubject(String type, String name, String url) {
        this.type = type;
        this.name = name;
        this.url = url;
    }

    public InviteSubject<T> with(T table) {
        return new InviteSubject<>(this.type, table.getName(), table.getUrl());
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
