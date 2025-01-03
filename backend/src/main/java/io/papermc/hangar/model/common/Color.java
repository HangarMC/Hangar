package io.papermc.hangar.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Color {

    FUCHSIA("#d946ef"),
    PURPLE("#a855f7"),
    VIOLET("#8b5cf6"),
    INDIGO("#6366f1"),
    BLUE("#3b82f6"),
    SKY("#0ea5e9"),
    CYAN("#06b6d4"),
    TEAL("#14b8a6"),
    EMERALD("#34d399"),
    GREEN("#22c55e"),
    LIME("#84cc16"),
    YELLOW("#eab308"),
    AMBER("#f59e0b"),
    ORANGE("#f97316"),
    RED("#ef4444"),
    STONE("#78716c"),
    GRAY("#A9A9A9"),
    TRANSPARENT("transparent");

    private static final List<Color> CHANNEL_COLORS = Arrays.stream(values()).filter(c -> c.ordinal() <= 15).collect(Collectors.toList());

    private final String hex;

    Color(final String hex) {
        this.hex = hex;
    }

    @JsonValue
    public String getHex() {
        return this.hex;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Color getByHex(final String hex) {
        for (final Color color : VALUES) {
            if (color.hex.equalsIgnoreCase(hex)) {
                return color;
            }
        }
        return null;
    }

    public static List<Color> getNonTransparentValues() {
        return CHANNEL_COLORS;
    }

    private static final Color[] VALUES = values();

    public static Color[] getValues() {
        return VALUES;
    }
}
