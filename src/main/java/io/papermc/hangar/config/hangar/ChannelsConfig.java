package io.papermc.hangar.config.hangar;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import io.papermc.hangar.model.Color;

@Component
@ConfigurationProperties(prefix = "hangar.channels")
public class ChannelsConfig {
    private int maxNameLen = 15;
    private String nameRegex = "^[a-zA-Z0-9]+$";
    @Value("#{T(io.papermc.hangar.model.Color).getById(${hangar.channels.color-default})}")
    private Color colorDefault = Color.getById(7);
    private String nameDefault = "Release";

    public int getMaxNameLen() {
        return maxNameLen;
    }

    public void setMaxNameLen(int maxNameLen) {
        this.maxNameLen = maxNameLen;
    }

    public String getNameRegex() {
        return nameRegex;
    }

    public void setNameRegex(String nameRegex) {
        this.nameRegex = nameRegex;
    }

    public Color getColorDefault() {
        return colorDefault;
    }

    public void setColorDefault(Color colorDefault) {
        this.colorDefault = colorDefault;
    }

    public String getNameDefault() {
        return nameDefault;
    }

    public void setNameDefault(String nameDefault) {
        this.nameDefault = nameDefault;
    }

    public boolean isValidChannelName(String name) {
        return name.length() >= 1 && name.length() <= maxNameLen && name.matches(nameRegex);
    }
}
