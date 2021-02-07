package io.papermc.hangar.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@JsonFormat(shape = Shape.OBJECT)
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

    public String getHex() {
        return hex;
    }

    @JsonCreator
    public static Color getByIdAndHex(@JsonProperty("hex") String hex, @JsonProperty("value") int id) {
        Color color = VALUES[id];
        if (color.hex.equalsIgnoreCase(hex)) {
            return color;
        }
        return null;
    }

    @JsonCreator
    public static Color getById(@JsonProperty("value") int id) {
        return VALUES[id];
    }

    public static Color getByHexStr(String hexStr) {
        for (Color value : VALUES) {
            if (value.hex.equalsIgnoreCase(hexStr)) return value;
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
