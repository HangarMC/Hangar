package io.papermc.hangar.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void testFormatVersionNumbers() {
        testFormatVersionNumbers("1.15-1.17", "1.15", "1.17", "1.16");
        testFormatVersionNumbers("1.15-1.17", "1.15", "1.17", "1.16");
        testFormatVersionNumbers("1.13, 1.15-1.17", "1.15", "1.17", "1.16", "1.13");
        testFormatVersionNumbers("1.13", "1.13");
        // this is a bit meh, but we have no reason to overthink this
        testFormatVersionNumbers("1.13, 1.15.2, 1.16, 1.17.0-1.17.1", "1.15.2", "1.17.1", "1.17.0", "1.16", "1.13");
    }

    private void testFormatVersionNumbers(String expected, String... versions) {
        assertEquals(expected, StringUtils.formatVersionNumbers(new ArrayList<>(List.of(versions))));
    }
}
