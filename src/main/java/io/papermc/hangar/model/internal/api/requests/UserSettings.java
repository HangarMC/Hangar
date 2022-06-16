package io.papermc.hangar.model.internal.api.requests;

public class UserSettings {

    private String theme;
    private String language;

    public UserSettings(String theme, String language) {
        this.theme = theme;
        this.language = language;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "UserSettings{" +
               "theme='" + theme + '\'' +
               ", language='" + language + '\'' +
               '}';
    }
}
