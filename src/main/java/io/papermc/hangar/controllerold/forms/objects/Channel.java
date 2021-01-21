package io.papermc.hangar.controllerold.forms.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.modelold.Color;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Channel {

    @JsonProperty(value = "color", required = true)
    private Color color;

    @JsonProperty(value = "name", required = true)
    private String name;

    @JsonProperty(value = "nonReviewed")
    private boolean nonReviewed = false;

    @NotNull
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Channel channel = (Channel) o;
        return nonReviewed == channel.nonReviewed &&
                color == channel.color &&
                name.equals(channel.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, name, nonReviewed);
    }

    public boolean isNonReviewed() {
        return nonReviewed;
    }

    public void setNonReviewed(boolean nonReviewed) {
        this.nonReviewed = nonReviewed;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "color=" + color +
                ", name='" + name + '\'' +
                ", nonReviewed=" + nonReviewed +
                '}';
    }
}
