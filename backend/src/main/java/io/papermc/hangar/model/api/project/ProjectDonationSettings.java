package io.papermc.hangar.model.api.project;

import java.util.StringJoiner;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectDonationSettings {

    private final boolean enable;
    private final String subject;

    @JdbiConstructor
    public ProjectDonationSettings(final boolean enabled, final String subject) {
        this.enable = enabled;
        this.subject = subject;
    }

    public boolean isEnable() {
        return this.enable;
    }

    public String getSubject() {
        return this.subject;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ProjectDonationSettings.class.getSimpleName() + "[", "]")
            .add("enable=" + this.enable)
            .add("email='" + this.subject + "'")
            .toString();
    }
}
