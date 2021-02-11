package io.papermc.hangar.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Color {

    PURPLE("#B400FF"),
    VIOLET("#C87DFF"),
    MAGENTA("#E100E1"),
    BLUE("#0000FF"),
    LIGHTBLUE("#B9F2FF"),
    QUARTZ("#E7FEFF"),
    AQUA("#0096FF"),
    CYAN("#00E1E1"),
    GREEN("#00DC00"),
    DARKGREEN("#009600"),
    CHARTREUSE("#7FFF00"),
    AMBER("#FFC800"),
    GOLD("#CFB53B"),
    ORANGE("#FF8200"),
    RED("#DC0000"),
    SILVER("#C0C0C0"),
    GRAY("#A9A9A9"),
    TRANSPARENT("transparent");

    private static List<Color> CHANNEL_COLORS = null;

    private final String hex;

    Color(String hex) {
        this.hex = hex;
    }

    @JsonValue
    public String getHex() {
        return hex;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static Color getByHex(String hex) {
        for (Color color : VALUES) {
            if (color.hex.equalsIgnoreCase(hex)) {
                return color;
            }
        }
        return null;
    }

    public static List<Color> getNonTransparentValues() {
        if (CHANNEL_COLORS == null) {
            CHANNEL_COLORS = Arrays.stream(VALUES).filter(c -> c.ordinal() <= 15).collect(Collectors.toList());
        }
        return CHANNEL_COLORS;
    }

    private static final Color[] VALUES = values();

    public static Color[] getValues() {
        return VALUES;
    }
}
