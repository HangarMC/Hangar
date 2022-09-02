package io.papermc.hangar.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class FormattedVersionsTest {

    @Test
    void testFormattedVersions() {
        List<String> list1 = List.of("1.1", "1.2", "1.3", "1.5", "1.7", "1.8");

        Assertions.assertEquals("1.1-1.3, 1.5, 1.7-1.8", StringUtils.formatVersionNumbers(new ArrayList<>(list1)));

        List<String> list2 = List.of("1.20", "1.23", "1.25", "1.30", "1.31");
        Assertions.assertEquals("1.20, 1.23, 1.25, 1.30-1.31", StringUtils.formatVersionNumbers(new ArrayList<>(list2)));

        List<String> list3 = List.of("1.1.0", "1.1.1", "1.2.0", "1.2.2", "1.3", "1.4");
        Assertions.assertEquals("1.1.0-1.1.1, 1.2.0, 1.2.2, 1.3-1.4", StringUtils.formatVersionNumbers(new ArrayList<>(list3)));

        List<String> list4 = List.of("1.1", "1.2", "1.3", "1.4", "1.5", "1.7", "1.8", "1.9", "1.10", "1.12");
        Assertions.assertEquals("1.1-1.5, 1.7-1.10, 1.12", StringUtils.formatVersionNumbers(new ArrayList<>(list4)));
    }
}
