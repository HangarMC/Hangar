package io.papermc.hangar.model.internal.api.requests;

public class UserSettings {

    private String theme;
    private String language;

    public UserSettings(final String theme, final String language) {
        this.theme = theme;
        this.language = language;
    }

    public String getTheme() {
        return this.theme;
    }

    public void setTheme(final String theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "UserSettings{" +
            "theme='" + this.theme + '\'' +
            ", language='" + this.language + '\'' +
            '}';
    }
}
