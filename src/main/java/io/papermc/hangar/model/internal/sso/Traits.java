package io.papermc.hangar.model.internal.sso;

import java.util.Objects;

public class Traits {

    private String email;
    private String username;
    private String discord;
    private String github;
    private String minecraft;
    private String language;
    private String theme;
    private Name name;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDiscord() {
        return discord;
    }

    public void setDiscord(String discord) {
        this.discord = discord;
    }

    public String getGithub() {
        return github;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    public String getMinecraft() {
        return minecraft;
    }

    public void setMinecraft(String minecraft) {
        this.minecraft = minecraft;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    @Override
    public String toString() {
        return "Traits{" +
               "email='" + email + '\'' +
               ", username='" + username + '\'' +
               ", discord='" + discord + '\'' +
               ", github='" + github + '\'' +
               ", minecraft='" + minecraft + '\'' +
               ", name=" + name +
               ", language=" + language +
               ", theme=" + theme +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Traits traits = (Traits) o;
        return Objects.equals(email, traits.email) && Objects.equals(username, traits.username) && Objects.equals(discord, traits.discord) && Objects.equals(github, traits.github) && Objects.equals(minecraft, traits.minecraft) && Objects.equals(name, traits.name) && Objects.equals(theme, traits.theme) && Objects.equals(language, traits.language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, username, discord, github, minecraft, name, language, theme);
    }

    public static class Name {
        private String first;
        private String last;

        public String getFirst() {
            return first;
        }

        public void setFirst(String first) {
            this.first = first;
        }

        public String getLast() {
            return last;
        }

        public void setLast(String last) {
            this.last = last;
        }

        @Override
        public String toString() {
            return "Name{" +
                   "first='" + first + '\'' +
                   ", last='" + last + '\'' +
                   '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Name name = (Name) o;
            return Objects.equals(first, name.first) && Objects.equals(last, name.last);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, last);
        }
    }
}
