package io.papermc.hangar.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

@JsonFormat(shape = Shape.OBJECT)
public enum Color {

    PURPLE(0, "#B400FF"),
    VIOLET(1, "#C87DFF"),
    MAGENTA(2, "#E100E1"),
    BLUE(3, "#0000FF"),
    LIGHTBLUE(4, "#B9F2FF"),
    QUARTZ(5, "#E7FEFF"),
    AQUA(6, "#0096FF"),
    CYAN(7, "#00E1E1"),
    GREEN(8, "#00DC00"),
    DARKGREEN(9, "#009600"),
    CHARTREUSE(10, "#7FFF00"),
    AMBER(11, "#FFC800"),
    GOLD(12, "#CFB53B"),
    ORANGE(13, "#FF8200"),
    RED(14, "#DC0000"),
    SILVER(15, "#C0C0C0"),
    GRAY(16, "#A9A9A9"),
    TRANSPARENT(17, "transparent");

    private static final Color[] VALUES = values();
    private final int value;
    private final String hex;

    Color(int value, String hex) {
        this.value = value;
        this.hex = hex;
    }

    public int getValue() {
        return value;
    }

    public String getHex() {
        return hex;
    }

    public static Color getById(int id) {
        return VALUES[id];
    }

    public static Color getByHexStr(String hexStr) {
        for (Color value : VALUES) {
            if (value.hex.equalsIgnoreCase(hexStr)) return value;
        }
        return null;
    }
}
