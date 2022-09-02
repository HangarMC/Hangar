package io.papermc.hangar.model.api.project;

import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.util.List;
import java.util.StringJoiner;

public class ProjectDonationSettings {

    private final boolean enable;
    private final String subject;

    @JdbiConstructor
    public ProjectDonationSettings(boolean enabled, String subject) {
        this.enable = enabled;
        this.subject = subject;
    }

    public boolean isEnable() {
        return enable;
    }

    public String getSubject() {
        return subject;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ProjectDonationSettings.class.getSimpleName() + "[", "]")
                .add("enable=" + enable)
                .add("email='" + subject + "'")
                .toString();
    }
}
