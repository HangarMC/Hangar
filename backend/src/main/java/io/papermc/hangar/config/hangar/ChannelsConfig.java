package io.papermc.hangar.config.hangar;

import io.papermc.hangar.model.common.Color;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Size;

@Component
@ConfigurationProperties(prefix = "hangar.channels")
public class ChannelsConfig {
    @Size(min = 1)
    private int maxNameLen = 15;
    private String nameRegex = "^[a-zA-Z0-9]+$";
    private Color colorDefault = Color.CYAN;
    @Size(min = 1, max = 15)
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
