package io.papermc.hangar.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class FormattedVersionsTest {
    private final List<String> versions = List.of("1.11", "1.11.1", "1.11.2", "1.11.3", "1.12", "1.13", "1.14", "1.14.1", "1.15", "1.15.1", "1.18");

    @Test
    void testFormattedVersions() {
        this.testFormatVersionNumbers("1.11-1.11.2, 1.15-1.18", "1.11", "1.11.1", "1.11.2", "1.15", "1.15.1", "1.18");
        this.testFormatVersionNumbers("1.11-1.11.2, 1.15, 1.18", "1.11", "1.11.1", "1.11.2", "1.15", "1.18");
        this.testFormatVersionNumbers("1.11.1-1.11.2", "1.11.1", "1.11.2");
        this.testFormatVersionNumbers("1.11, 1.11.2, 1.15.1", "1.11", "1.11.2", "1.15.1");
        this.testFormatVersionNumbers("1.11, 1.12-1.13", "1.11", "1.12", "1.13");
    }

    private void testFormatVersionNumbers(final String expected, final String... versions) {
        Assertions.assertEquals(expected, VersionFormatter.formatVersionRange(new ArrayList<>(List.of(versions)), this.versions));
    }
}
